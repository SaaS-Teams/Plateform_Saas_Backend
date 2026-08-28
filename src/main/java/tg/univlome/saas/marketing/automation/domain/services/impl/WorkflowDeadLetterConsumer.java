package tg.univlome.saas.marketing.automation.domain.services.impl;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;
import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;
import tg.univlome.saas.marketing.automation.domain.models.WorkflowExecutionLog;
import tg.univlome.saas.marketing.automation.repositories.WorkflowExecutionLogRepository;

/**
 * Consumer de la Dead Letter Queue (DLQ) du module d'automatisation.
 *
 * <p>Les messages arrivent ici uniquement lorsque le {@link WorkflowConsumer}
 * principal les a rejetés définitivement ({@code basicNack(tag, false, false)}),
 * c'est-à-dire après une erreur fatale et non récupérable dans le moteur
 * d'exécution.</p>
 *
 * <h3>Responsabilités</h3>
 * <ol>
 *   <li>Marquer l'exécution correspondante en statut {@code FAILED} en base de données.</li>
 *   <li>Enregistrer les détails de l'échec (nœud en cours, horodatage, raison).</li>
 *   <li>Logger de manière explicite toutes les informations utiles pour le débogage.</li>
 * </ol>
 *
 * <h3>Politique de requeue</h3>
 * <p>Cette queue est un <em>cimetière de messages</em>. Un message ne doit
 * <strong>jamais</strong> être requeueé depuis la DLQ (ce serait une boucle infinie).
 * Tous les chemins d'exécution se terminent par {@code basicAck} ou
 * {@code basicNack(tag, false, <strong>false</strong>)}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowDeadLetterConsumer {

    private final WorkflowExecutionLogRepository executionLogRepository;

    /**
     * Traite un message mort reçu depuis la Dead Letter Queue.
     *
     * <p>Le mode d'acquittement global est {@code MANUAL} (configuré dans
     * {@code application.properties}). Ce consumer gère donc explicitement
     * le {@link Channel} pour garantir qu'aucun message n'est requeueé,
     * quelle que soit l'issue du traitement.</p>
     *
     * @param message le message de l'étape de workflow qui a échoué
     * @param channel le canal AMQP pour l'acquittement manuel
     * @param tag     le delivery tag du message
     * @throws IOException si l'appel à basicAck/basicNack échoue au niveau réseau
     */
    @RabbitListener(queues = "${saas.rabbitmq.queue.dlq}", ackMode = "MANUAL")
    public void processFailedMessage(
            WorkflowStepMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.error("╔══════════════════════════════════════════════════════════════");
        log.error("║ MESSAGE MORT REÇU EN DLQ");
        log.error("║ Exécution   : {}", message.executionTrackingId());
        log.error("║ Nœud échoué : {} (type: {})", message.nodeId(), message.actionType());
        log.error("╚══════════════════════════════════════════════════════════════");

        try {
            // Recherche de l'exécution en base de données
            WorkflowExecutionLog executionLog = executionLogRepository
                    .findByExecutionTrackingId(message.executionTrackingId())
                    .orElse(null);

            if (executionLog == null) {
                // L'exécution est introuvable (suppression manuelle, données corrompues, etc.)
                // On log et on abandonne proprement : pas de requeue.
                log.error("DLQ — Exécution introuvable en base pour le trackingId [{}]. "
                        + "Impossible de mettre à jour le statut. Message abandonné.",
                        message.executionTrackingId());
                channel.basicAck(tag, false);
                return;
            }

            // Informations de contexte pour le log détaillé
            String workflowName = executionLog.getWorkflow() != null
                    ? executionLog.getWorkflow().getName()
                    : "inconnu";
            String contactEmail = executionLog.getContact() != null
                    ? executionLog.getContact().getEmail()
                    : "inconnu";

            log.error("DLQ — Détails de l'échec :");
            log.error("  > Scénario  : '{}' (id: {})", workflowName,
                    executionLog.getWorkflow() != null
                            ? executionLog.getWorkflow().getTrackingId() : "N/A");
            log.error("  > Contact   : {} (id: {})", contactEmail,
                    executionLog.getContact() != null
                            ? executionLog.getContact().getId() : "N/A");
            log.error("  > Nœud      : {}", message.nodeId());
            log.error("  > Statut actuel avant correction : {}", executionLog.getStatus());

            // Mise à jour du statut en base : FAILED + horodatage + nœud fautif
            executionLog.setStatus(ExecutionStatus.FAILED);
            executionLog.setErrorDetails(
                    String.format("Échec définitif traité par la DLQ. Nœud fautif : [%s] (type: %s). "
                                    + "Vérifier les logs de l'application à l'heure de l'incident.",
                            message.nodeId(), message.actionType()));
            executionLog.setCurrentNodeId(message.nodeId());
            executionLog.setCompletedAt(LocalDateTime.now());

            executionLogRepository.save(executionLog);

            log.error("DLQ — Exécution [{}] marquée FAILED en base. Traitement terminé.",
                    message.executionTrackingId());

            // Acquittement final : le message est consommé et supprimé de la DLQ
            channel.basicAck(tag, false);

        } catch (Exception e) {
            // Le consumer DLQ lui-même a planté (ex: base de données indisponible).
            // On rejette SANS requeue : il n'existe pas de "DLQ de la DLQ".
            // L'alerte sera visible dans les logs et le monitoring.
            log.error("DLQ — Erreur critique lors du traitement du message mort [{}]. "
                    + "Message abandonné définitivement (requeue=false). Cause : {}",
                    message.executionTrackingId(), e.getMessage(), e);

            // requeue = false : jamais de boucle infinie depuis la DLQ
            channel.basicNack(tag, false, false);
        }
    }
}

package tg.univlome.saas.marketing.automation.domain.services.impl;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;
import tg.univlome.saas.marketing.automation.domain.models.WorkflowExecutionLog;
import tg.univlome.saas.marketing.automation.domain.services.RateLimitingService;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowEngineService;
import tg.univlome.saas.marketing.automation.repositories.WorkflowExecutionLogRepository;
@Slf4j
@Component // Component ou Service, les deux fonctionnent pour un Listener
@RequiredArgsConstructor
public class WorkflowConsumer {
    private static final int DEFAULT_RATE_LIMIT = 50;
    private static final int REQUEUE_DELAY_MS = 2000;

    private final WorkflowEngineService engineService;
    private final RateLimitingService rateLimitingService;
    private final WorkflowExecutionLogRepository executionLogRepository;

    @RabbitListener(queues = "${saas.rabbitmq.queue.workflow-step:workflow-step-queue}", ackMode = "MANUAL")
    public void receiveStepMessage(
            WorkflowStepMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.info("Message intercepté par RabbitMQ -> Exécution : {}", message.executionTrackingId());

        try {
            // 1. LE VIDEUR : Vérification de la limite avec le trackingId du Workflow
            WorkflowExecutionLog executionLog = executionLogRepository.findByExecutionTrackingId(message.executionTrackingId())
                    .orElseThrow(() -> new IllegalArgumentException("Exécution introuvable : " + message.executionTrackingId()));
            
            String workflowTrackingId = executionLog.getWorkflow().getTrackingId().toString();

            // Plus tard, cette limite sera lue en base de données selon l'abonnement du client
            if (!rateLimitingService.isAllowed(workflowTrackingId, DEFAULT_RATE_LIMIT)) {

                log.warn("Rate limit atteint pour le workflow [{}], requeue du message.", workflowTrackingId);

                // FEU ROUGE : On remet le message à la fin de la file (requeue = true)
                channel.basicNack(tag, false, true);

                // On endort le consumer pour éviter qu'il ne sature ton CPU
                Thread.sleep(REQUEUE_DELAY_MS);
                return;
            }

            // 2. FEU VERT : On traite le message
            engineService.processStep(message);

            // 3. SUCCÈS : On dit à RabbitMQ qu'il peut supprimer définitivement le message
            channel.basicAck(tag, false);

        } catch (InterruptedException e) {
            // Restaure le statut d'interruption du thread
            Thread.currentThread().interrupt();
            channel.basicNack(tag, false, true);

        } catch (Exception e) {
            log.error("Erreur fatale dans le Consumer pour l'exécution {}", message.executionTrackingId(), e);

            // EN CAS DE CRASH DU CODE : On rejette le message et on le supprime (requeue = false)
            // Sinon, l'erreur va boucler à l'infini et faire planter l'application.
            channel.basicNack(tag, false, false);
        }
    }
}

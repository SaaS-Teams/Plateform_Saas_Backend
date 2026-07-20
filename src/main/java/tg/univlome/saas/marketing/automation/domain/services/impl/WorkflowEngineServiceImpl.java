package tg.univlome.saas.marketing.automation.domain.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowNode;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;
import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;
import tg.univlome.saas.marketing.automation.domain.models.WorkflowExecutionLog;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowEngineService;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowProducerService;
import tg.univlome.saas.marketing.automation.repositories.WorkflowExecutionLogRepository;
import tg.univlome.saas.marketing.email.application.dtos.requests.EmailMessage;
import tg.univlome.saas.marketing.email.domain.services.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    private final WorkflowExecutionLogRepository executionRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final WorkflowProducerService producerService;

    // Injecter les services externes (comme EmailService) quand on voudra exécuter de vraies actions

    @Override
    @Transactional
    public void processStep(WorkflowStepMessage message) {
        log.info("Démarrage du traitement de l'étape [{}] pour l'exécution [{}]", message.nodeId(), message.executionTrackingId());

        // 1. Récupérer l'exécution en cours
        WorkflowExecutionLog execution = executionRepository.findByExecutionTrackingId(message.executionTrackingId())
                .orElseThrow(() -> new IllegalArgumentException("Exécution introuvable"));

        // Si le parcours est annulé ou en erreur, on stoppe tout
        if (execution.getStatus() != ExecutionStatus.IN_PROGRESS && execution.getStatus() != ExecutionStatus.PENDING) {
            log.warn("Exécution {} ignorée car son statut est {}", execution.getExecutionTrackingId(), execution.getStatus());
            return;
        }

        // On passe en IN_PROGRESS si ce n'était pas le cas (ex: au tout premier nœud)
        execution.setStatus(ExecutionStatus.IN_PROGRESS);

        try {
            // 2. Extraire les nœuds du JSON (flowData)
            String flowDataJson = execution.getWorkflow().getFlowData();
            WorkflowNode currentNode = findNodeInFlowData(flowDataJson, message.nodeId());

            if (currentNode == null) {
                // Nœud introuvable
                throw new IllegalArgumentException("Nœud introuvable dans le scénario : " + message.nodeId());
            }

            // 3. Exécuter l'action selon le type du Nœud
            executeNodeAction(currentNode, execution);

            // 4. Préparer et déclencher l'étape suivante (L'Effet Domino)
            String nextStepId = currentNode.nextStepId();
            execution.setCurrentNodeId(nextStepId);

            if (nextStepId != null && !nextStepId.trim().isEmpty()) {
                // Il y a une suite ! On sauvegarde l'état actuel...
                executionRepository.save(execution);

                // ... ET on envoie un nouveau message à RabbitMQ pour l'étape suivante.
                // Cela relance automatiquement la boucle de l'automatisation.
                WorkflowStepMessage nextMessage = new WorkflowStepMessage(
                        execution.getExecutionTrackingId(),
                        nextStepId,
                        "AUTO_CONTINUE"
                );
                producerService.sendStepToQueue(nextMessage);
                log.info("-> Étape suivante [{}] envoyée dans la file d'attente RabbitMQ.", nextStepId);

            } else {
                // Si nextStepId est null ou vide, c'est la fin du parcours.
                completeExecution(execution);
            }

        } catch (Exception e) {
            log.error("Erreur critique lors de l'exécution du nœud", e);
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setErrorDetails(e.getMessage());
            executionRepository.save(execution);
        }
    }

    // --- Méthodes privées d'aide ---

    private WorkflowNode findNodeInFlowData(String json, String nodeId) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode nodesArray = root.get("nodes");

        if (nodesArray == null || !nodesArray.isArray()) {
            return null;
        }

        List<WorkflowNode> nodes = objectMapper.convertValue(nodesArray, new TypeReference<>() {});

        return nodes.stream()
                // Ton adaptation parfaite avec nodeId()
                .filter(n -> n.nodeId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    private void executeNodeAction(WorkflowNode node, WorkflowExecutionLog execution) {
        log.info("Exécution de l'action de type : {}", node.type());

        switch (node.type()) {
            case "ACTION_EMAIL":
                // 1. Extraction des paramètres depuis le dessin du front-end
                String templateId = (String) node.data().get("templateId");
                String subject = (String) node.data().getOrDefault("subject", "Nouveau message pour vous");

                // 2. Récupération de l'e-mail du contact (je suppose getEmail() sur ton entité Contact)
                String contactEmail = execution.getContact().getEmail();

                log.info("-> Envoi RÉEL d'un email (Sujet: {}) au contact {}", subject, contactEmail);

                // 3. Création du DTO attendu par ton module Email
                // (Si ton EmailMessage utilise un builder, tu peux utiliser EmailMessage.builder()...)
                EmailMessage emailMsg = new EmailMessage(
                        contactEmail,
                        subject,
                        "Ceci est le contenu généré par le scénario. Template: " + templateId,
                        false
                );

                // 4. Appel du vrai service (qui utilise SendGrid en arrière-plan selon ton architecture)
                emailService.sendEmail(emailMsg);
                break;

            case "WAIT":
                Integer days = (Integer) node.data().get("days");
                log.info("-> [Simulation] Mise en attente de {} jours (nécessitera un Quartz/CRON plus tard)", days);
                break;

            default:
                log.warn("Type de nœud inconnu : {}", node.type());
        }
    }

    private void completeExecution(WorkflowExecutionLog execution) {
        log.info("Exécution {} terminée avec succès. Fin du parcours.", execution.getExecutionTrackingId());
        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setCurrentNodeId(null);
        executionRepository.save(execution);
    }
}

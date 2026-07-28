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
import tg.univlome.saas.marketing.automation.domain.services.ConditionEvaluatorService;
import tg.univlome.saas.marketing.automation.domain.services.SmsService;
import tg.univlome.saas.marketing.automation.domain.services.WebhookService;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowEngineService;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowProducerService;
import tg.univlome.saas.marketing.automation.repositories.WorkflowExecutionLogRepository;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.domain.models.ContactSegment;
import tg.univlome.saas.marketing.contact.domain.models.Segment;
import tg.univlome.saas.marketing.contact.repositories.ContactSegmentRepository;
import tg.univlome.saas.marketing.contact.repositories.SegmentRepository;
import tg.univlome.saas.marketing.email.application.dtos.requests.EmailMessage;
import tg.univlome.saas.marketing.email.domain.services.EmailService;
import tg.univlome.saas.shared.exceptions.ResourceNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    private final WorkflowExecutionLogRepository executionRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final WorkflowProducerService producerService;
    private final ConditionEvaluatorService conditionEvaluator;
    private final WorkflowSchedulingService schedulingService;
    private final SmsService smsService;
    private final WebhookService webhookService;
    private final SegmentRepository segmentRepository;
    private final ContactSegmentRepository contactSegmentRepository;

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
            //    Pour un nœud CONDITION, le prochain nœud dépend du résultat de l'évaluation.
            //    Pour les autres types, on utilise le chaînage séquentiel classique (nextStepId).
            String nextStepId = resolveNextStepId(currentNode, execution);
            execution.setCurrentNodeId(nextStepId);

            if (nextStepId != null && !nextStepId.trim().isEmpty()) {
                executionRepository.save(execution);

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
                // Lecture de la durée définie dans le nœud (champ "days" du JSON)
                int waitDays = node.data().containsKey("days")
                        ? Integer.parseInt(String.valueOf(node.data().get("days")))
                        : 1;

                // Identifier le prochain nœud à exécuter après l'expiration du délai
                String nextNodeId = node.nextStepId();
                if (nextNodeId == null || nextNodeId.isBlank()) {
                    throw new ResourceNotFoundException(
                            "Le nœud WAIT [" + node.nodeId() + "] n'a pas de nœud suivant défini.");
                }

                // Construire le message de reprise qui sera resoumis dans RabbitMQ après X jours
                WorkflowStepMessage resumeMessage = new WorkflowStepMessage(
                        execution.getExecutionTrackingId(),
                        nextNodeId,
                        "WAIT_RESUME"
                );

                // Planifier la reprise via Quartz (persisté en BDD, survit au redémarrage)
                try {
                    schedulingService.scheduleWaitNode(resumeMessage, waitDays);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Échec de la planification Quartz pour le nœud WAIT : " + e.getMessage(), e);
                }
                break;

            case "CONDITION":
                // Pas d'action à exécuter ici : le nœud CONDITION est un aiguillage pur.
                // L'évaluation et le routage sont gérés par resolveNextStepId().
                log.info("-> Nœud conditionnel [{}] détecté. Évaluation déléguée au routage.", node.nodeId());
                break;

            case "TAG_CONTACT":
                String tagName = (String) node.data().get("tagName");
                Contact contactToTag = execution.getContact();
                log.info("-> Nœud TAG_CONTACT : ajout du tag/segment '{}' au contact [{}]", tagName, contactToTag.getId());
                
                if (tagName != null && !tagName.isBlank()) {
                    Segment segment = segmentRepository.findByName(tagName)
                            .orElseGet(() -> {
                                Segment newSeg = new Segment();
                                newSeg.setName(tagName);
                                newSeg.setDescription("Segment/Tag généré par workflow");
                                return segmentRepository.save(newSeg);
                            });

                    boolean alreadyTagged = contactSegmentRepository
                            .existsByContactTrackingIdAndSegmentTrackingId(contactToTag.getTrackingId(), segment.getTrackingId());

                    if (!alreadyTagged) {
                        ContactSegment cs = new ContactSegment(contactToTag, segment);
                        contactSegmentRepository.save(cs);
                        log.info("-> Tag '{}' associé avec succès au contact [{}]", tagName, contactToTag.getId());
                    } else {
                        log.info("-> Le contact [{}] possède déjà le tag '{}'", contactToTag.getId(), tagName);
                    }
                }
                break;

            case "SMS":
                String phone = (String) node.data().getOrDefault("phone", "");
                String smsText = (String) node.data().getOrDefault("message", "Message automatique de votre workflow");
                log.info("-> Nœud SMS : envoi vers [{}]", phone);
                smsService.sendSms(phone, smsText);
                break;

            case "WEBHOOK":
                String webhookUrl = (String) node.data().get("url");
                log.info("-> Nœud WEBHOOK : déclenchement vers [{}]", webhookUrl);
                webhookService.triggerWebhook(webhookUrl, node.data());
                break;

            default:
                log.warn("Type de nœud inconnu : {}", node.type());
        }
    }

    /**
     * Détermine l'identifiant du prochain nœud à exécuter immédiatement.
     *
     * <p>Cas spéciaux qui court-circuitent le chaînage immédiat :</p>
     * <ul>
     *   <li>{@code CONDITION} : évalue la condition et retourne la branche IF ou ELSE.</li>
     *   <li>{@code WAIT} : retourne {@code null} — le chaînage est délégué à Quartz
     *       qui remettra le message dans RabbitMQ après le délai défini.</li>
     * </ul>
     *
     * @param node      le nœud courant
     * @param execution le journal d'exécution (contient le contact)
     * @return l'identifiant du prochain nœud, ou {@code null} si fin de parcours ou WAIT en attente
     */
    private String resolveNextStepId(WorkflowNode node, WorkflowExecutionLog execution) {
        if ("CONDITION".equals(node.type())) {
            String contactId = execution.getContact().getId().toString();
            boolean conditionResult = conditionEvaluator.evaluateCondition(node.data(), contactId);
            String nextId = conditionResult ? node.nextStepIdIfTrue() : node.nextStepIdIfFalse();
            log.info("-> Résultat de la condition : {} — Prochain nœud : [{}]",
                    conditionResult ? "VRAI (IF)" : "FAUX (ELSE)", nextId);
            return nextId;
        }

        if ("WAIT".equals(node.type())) {
            // Quartz prend le relais : le moteur ne doit PAS envoyer de message immédiatement.
            // La reprise sera déclenchée par WorkflowResumeJob après expiration du délai.
            log.info("-> Nœud WAIT [{}] : chaînage suspendu, reprise planifiée via Quartz.",
                    node.nodeId());
            return null;
        }

        // Chaînage séquentiel classique pour les autres nœuds
        return node.nextStepId();
    }


    private void completeExecution(WorkflowExecutionLog execution) {
        log.info("Exécution {} terminée avec succès. Fin du parcours.", execution.getExecutionTrackingId());
        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setCurrentNodeId(null);
        executionRepository.save(execution);
    }
}

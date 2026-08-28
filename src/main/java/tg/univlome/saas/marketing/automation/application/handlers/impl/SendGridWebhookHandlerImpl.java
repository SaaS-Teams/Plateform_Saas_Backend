package tg.univlome.saas.marketing.automation.application.handlers.impl;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.automation.application.handlers.WebhookPayloadHandler;
import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;
import tg.univlome.saas.marketing.automation.domain.models.WorkflowExecutionLog;
import tg.univlome.saas.marketing.automation.repositories.WorkflowExecutionLogRepository;

/**
 * Stratégie métier de traitement des webhooks SendGrid (Bounces, Open, Click, Delivered, etc.).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendGridWebhookHandlerImpl implements WebhookPayloadHandler {

    private static final String PROVIDER_NAME = "sendgrid";
    private final WorkflowExecutionLogRepository executionLogRepository;

    @Override
    public boolean supports(String provider) {
        return PROVIDER_NAME.equalsIgnoreCase(provider);
    }

    @Override
    @Transactional
    public void process(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            log.warn("[SENDGRID HANDLER] Payload nul ou vide reçu. Ignoré.");
            return;
        }

        String eventType = (String) payload.getOrDefault("event", "unknown");
        String rawExecutionId = extractExecutionId(payload);

        log.info("[SENDGRID HANDLER] Événement SendGrid reçu : type='{}', executionId='{}'", eventType, rawExecutionId);

        if (rawExecutionId == null || rawExecutionId.isBlank()) {
            log.warn("[SENDGRID HANDLER] Aucun 'executionId' trouvé dans le payload SendGrid. "
                    + "Traitement analytique anonyme sans impact d'exécution.");
            return;
        }

        UUID executionTrackingId;
        try {
            executionTrackingId = UUID.fromString(rawExecutionId);
        } catch (IllegalArgumentException e) {
            log.error("[SENDGRID HANDLER] Identifiant 'executionId' invalide : [{}]", rawExecutionId);
            return;
        }

        Optional<WorkflowExecutionLog> executionOpt = executionLogRepository.findByExecutionTrackingId(executionTrackingId);

        if (executionOpt.isEmpty()) {
            log.warn("[SENDGRID HANDLER] Aucune exécution trouvée pour l'ID [{}]", executionTrackingId);
            return;
        }

        WorkflowExecutionLog execution = executionOpt.get();

        switch (eventType.toLowerCase()) {
            case "bounce":
            case "dropped":
                log.error("[SENDGRID HANDLER] Échec de livraison email pour l'exécution [{}] (event={})",
                        executionTrackingId, eventType);
                execution.setStatus(ExecutionStatus.FAILED);
                execution.setErrorDetails("Échec d'envoi d'email signalé par SendGrid (" + eventType + ").");
                executionLogRepository.save(execution);
                break;

            case "delivered":
                log.info("[SENDGRID HANDLER] Email livré avec succès pour l'exécution [{}]", executionTrackingId);
                break;

            case "open":
                log.info("[SENDGRID HANDLER] Ouverture d'email détectée pour l'exécution [{}]", executionTrackingId);
                break;

            case "click":
                log.info("[SENDGRID HANDLER] Clic sur un lien email détecté pour l'exécution [{}]", executionTrackingId);
                break;

            case "spamreport":
            case "unsubscribe":
                log.warn("[SENDGRID HANDLER] Signalement Spam/Désabonnement pour l'exécution [{}]", executionTrackingId);
                break;

            default:
                log.info("[SENDGRID HANDLER] Événement SendGrid non géré spécifiquement : '{}'", eventType);
                break;
        }
    }

    /**
     * Tente d'extraire la clé "executionId" directement du payload ou de la sous-map "custom_args".
     */
    @SuppressWarnings("unchecked")
    private String extractExecutionId(Map<String, Object> payload) {
        if (payload.containsKey("executionId")) {
            return String.valueOf(payload.get("executionId"));
        }
        if (payload.containsKey("custom_args") && payload.get("custom_args") instanceof Map) {
            Map<String, Object> customArgs = (Map<String, Object>) payload.get("custom_args");
            if (customArgs.containsKey("executionId")) {
                return String.valueOf(customArgs.get("executionId"));
            }
        }
        return null;
    }
}

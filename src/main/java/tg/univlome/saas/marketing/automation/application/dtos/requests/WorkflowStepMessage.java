package tg.univlome.saas.marketing.automation.application.dtos.requests;

import java.util.UUID;

public record WorkflowStepMessage(
        UUID executionTrackingId,  // L'ID du journal (pour savoir de quelle exécution on parle)
        String nodeId,             // L'étape exacte du JSON (ex: "node_email_3")
        String actionType        // Le type d'action (ex: "SEND_EMAIL", "WAIT", "CONDITION")
) {}

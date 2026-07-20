package tg.univlome.saas.marketing.automation.application.dtos.responses;

import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkflowExecutionResponse(
        // Uniquement l'UUID d'exécution
        UUID executionTrackingId,

        // On renvoie un résumé du workflow parent et l'ID du contact
        UUID workflowTrackingId,
        Long contactId,

        ExecutionStatus status,
        String currentNodeId,
        String errorDetails,

        LocalDateTime startedAt,
        LocalDateTime completedAt
) {}

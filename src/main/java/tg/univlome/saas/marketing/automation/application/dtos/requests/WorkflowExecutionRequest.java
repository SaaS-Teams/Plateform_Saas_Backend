package tg.univlome.saas.marketing.automation.application.dtos.requests;

import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkflowExecutionRequest(
        UUID workflowTrackingId,
        Long contactId,

        ExecutionStatus status,
        String currentNodeId,
        String errorDetails,

        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
}

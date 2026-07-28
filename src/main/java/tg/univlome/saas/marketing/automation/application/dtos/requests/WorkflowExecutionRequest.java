package tg.univlome.saas.marketing.automation.application.dtos.requests;


import java.time.LocalDateTime;
import java.util.UUID;
import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;

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

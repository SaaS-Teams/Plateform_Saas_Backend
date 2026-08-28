package tg.univlome.saas.marketing.automation.application.dtos.responses;



import java.time.LocalDateTime;
import java.util.UUID;
import tg.univlome.saas.marketing.automation.domain.enums.WorkflowStatus;

public record WorkflowResponse(
        UUID trackingId,
        String name,
        String description,
        WorkflowStatus status,
        String triggerType,
        String flowData,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

package tg.univlome.saas.marketing.automation.application.dtos.responses;

package tg.univlome.saas.marketing.automation.application.dtos.response;

import tg.univlome.saas.marketing.automation.domain.enums.WorkflowStatus;

import java.time.LocalDateTime;
import java.util.UUID;

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

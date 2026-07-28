package tg.univlome.saas.marketing.analytique.application.dtos.responses;

import java.util.UUID;

public record WorkflowAnalyticsResponse(
        UUID workflowTrackingId,
        long totalSent,
        long totalOpened,
        long totalClicked,
        long totalBounced,
        double openRate,
        double clickRate,
        double bounceRate
) {
}

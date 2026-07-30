package tg.univlome.saas.marketing.analytique.application.dtos.response;

import java.util.List;

public record DashboardResponse(
        long totalSent,
        long totalOpened,
        long totalClicked,
        long totalBounced,
        double openRate,
        double clickRate,
        List<DailyStats> chartData
) {
}

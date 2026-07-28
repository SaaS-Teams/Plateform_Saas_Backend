package tg.univlome.saas.marketing.analytique.application.dtos.response;

import java.util.List;

public record DashboardResponse(
        // --- Global KPIs ---
        long totalCampaigns,
        long draftCampaigns,
        long scheduledCampaigns,
        long sentCampaigns,

        long totalEmailsSent,
        long totalSuccessfulEmails,
        long totalFailedEmails,

        double overallSuccessRate,

        List<DailyStats> dailyStatsList
) {
}

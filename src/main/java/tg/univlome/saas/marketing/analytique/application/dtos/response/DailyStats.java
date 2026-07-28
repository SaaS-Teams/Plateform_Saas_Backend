package tg.univlome.saas.marketing.analytique.application.dtos.response;

import java.time.LocalDateTime;

public record DailyStats(
        LocalDateTime date,
        long successCount,
        long failureCount
) {
}

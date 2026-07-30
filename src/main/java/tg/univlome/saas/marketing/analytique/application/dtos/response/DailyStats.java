package tg.univlome.saas.marketing.analytique.application.dtos.response;

import java.time.LocalDate;

public record DailyStats(
        LocalDate date,
        long opens,
        long clicks
) {
}

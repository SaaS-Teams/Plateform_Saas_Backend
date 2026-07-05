package tg.univlome.saas.marketing.analytique.repositories.projections;

import java.time.LocalDateTime;

public interface DailyStatsProjection {
    LocalDateTime getDate();
    long getSuccessCount(); // Doit correspondre à l'alias SQL "as successCount"
    long getFailureCount(); // Doit correspondre à l'alias SQL "as failureCount"
}

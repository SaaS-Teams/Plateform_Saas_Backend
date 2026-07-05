package tg.univlome.saas.marketing.email.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.marketing.analytique.repositories.projections.DailyStatsProjection;
import tg.univlome.saas.marketing.email.domain.enums.EmailStatus;
import tg.univlome.saas.marketing.email.domain.models.EmailLog;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    long countByStatus(EmailStatus status);

    @Query(value = """
        SELECT 
            DATE(created_at) as date, 
            SUM(CASE WHEN status = 'SENT' THEN 1 ELSE 0 END) as successCount, 
            SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failureCount 
        FROM email_logs 
        GROUP BY DATE(created_at) 
        ORDER BY date ASC
            """, nativeQuery = true)
    List<DailyStatsProjection> getDailyStatistics();
}

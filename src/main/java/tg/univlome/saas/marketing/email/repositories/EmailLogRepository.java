package tg.univlome.saas.marketing.email.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.marketing.email.domain.models.EmailLog;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
}

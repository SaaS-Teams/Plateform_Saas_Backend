package tg.univlome.saas.marketing.contact.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.univlome.saas.marketing.contact.domain.models.ConsentLog;

import java.util.UUID;

public interface ConsentLogRepository extends JpaRepository<ConsentLog, Long> {

    Page<ConsentLog> findByContact_TrackingId(UUID contactTrackingId, Pageable pageable);
}

package tg.univlome.saas.marketing.contact.repositories;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.univlome.saas.marketing.contact.domain.models.ConsentLog;

@SuppressWarnings("checkstyle:MethodName") // Ajoute cette ligne
public interface ConsentLogRepository extends JpaRepository<ConsentLog, Long> {

    @SuppressWarnings("all")
    Page<ConsentLog> findByContactTrackingId(UUID contactTrackingId, Pageable pageable);
}

package tg.univlome.saas.marketing.contact.repositories;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.univlome.saas.marketing.contact.domain.models.ContactSegment;

public interface ContactSegmentRepository extends JpaRepository<ContactSegment, Long> {

    @SuppressWarnings("all")
        // On bloque toutes les vérifications juste pour cette ligne
    Page<ContactSegment> findBySegmentTrackingId(UUID segmentTrackingId, Pageable pageable);

    @SuppressWarnings("all")
    Page<ContactSegment> findByContactTrackingId(UUID contactTrackingId, Pageable pageable);

    @SuppressWarnings("all")
    boolean existsByContactTrackingIdAndSegmentTrackingId(UUID contactTrackingId, UUID segmentTrackingId);

    @SuppressWarnings("all")
    void deleteByContactTrackingIdAndSegmentTrackingId(UUID contactTrackingId, UUID segmentTrackingId);
}


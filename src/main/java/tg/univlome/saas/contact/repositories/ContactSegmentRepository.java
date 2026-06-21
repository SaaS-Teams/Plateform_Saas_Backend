package tg.univlome.saas.marketing.contact.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.univlome.saas.marketing.contact.domain.models.ContactSegment;

import java.util.UUID;

public interface ContactSegmentRepository extends JpaRepository<ContactSegment, Long> {

   Page<ContactSegment> findBySegment_TrackingId(UUID segmentTrackingId, Pageable pageable);

    Page<ContactSegment> findByContact_TrackingId(UUID contactTrackingId, Pageable pageable);

    boolean existsByContact_TrackingIdAndSegment_TrackingId(UUID contactTrackingId, UUID segmentTrackingId);

    void deleteByContact_TrackingIdAndSegment_TrackingId(UUID contactTrackingId, UUID segmentTrackingId);
}

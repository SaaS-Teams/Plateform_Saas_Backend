package tg.univlome.saas.marketing.contact.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.univlome.saas.marketing.contact.domain.models.Segment;

public interface SegmentRepository extends JpaRepository<Segment, Long> {

    Optional<Segment> findByTrackingId(UUID trackingId);

    Optional<Segment> findByName(String name);
}

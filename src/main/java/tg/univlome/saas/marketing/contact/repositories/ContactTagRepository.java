package tg.univlome.saas.marketing.contact.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.marketing.contact.domain.models.ContactTag;

@Repository
public interface ContactTagRepository extends JpaRepository<ContactTag, Long> {

    Optional<ContactTag> findByTrackingId(UUID trackingId);

    List<ContactTag> findByContactTrackingId(UUID contactTrackingId);

    boolean existsByContactTrackingIdAndTagTrackingId(UUID contactTrackingId, UUID tagTrackingId);

    void deleteByContactTrackingIdAndTagTrackingId(UUID contactTrackingId, UUID tagTrackingId);
}

package tg.univlome.saas.marketing.contact.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.marketing.contact.domain.models.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByTrackingId(UUID trackingId);

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    void deleteByTrackingId(UUID trackingId);
}

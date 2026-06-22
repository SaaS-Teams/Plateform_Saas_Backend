package tg.univlome.saas.marketing.contact.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;
import tg.univlome.saas.marketing.contact.domain.models.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findByTrackingId(UUID trackingId);

    Optional<Contact> findByEmail(String email);

    Page<Contact> findByConsentStatus(ConsentStatus status, Pageable pageable);

    void deleteByTrackingId(UUID trackingId);

    boolean existsByEmail(String email);
}

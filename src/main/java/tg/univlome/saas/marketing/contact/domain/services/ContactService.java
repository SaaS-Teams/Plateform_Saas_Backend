package tg.univlome.saas.marketing.contact.domain.services;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.univlome.saas.marketing.contact.application.dtos.request.ContactRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;

public interface ContactService {
    ContactResponse createContact(ContactRequest request, String ipAddress);

    ContactResponse updateContact(UUID trackingId, ContactRequest request);

    ContactResponse getContactByTrackingId(UUID trackingId);

    Page<ContactResponse> getAllContacts(Pageable pageable);

    ContactResponse changeConsentStatus(UUID trackingId, ConsentStatus newStatus, String ipAddress);
}

package tg.univlome.saas.marketing.contact.domain.services.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.contact.application.dtos.request.ContactRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.application.mappers.ContactMapper;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.domain.services.ConsentLogService;
import tg.univlome.saas.marketing.contact.domain.services.ContactService;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final ConsentLogService consentLogService;

    @Override
    @Transactional
    public ContactResponse createContact(ContactRequest request, String ipAddress) {
        // 1. Règle métier : Déduplication
        if (contactRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Un contact avec cet email existe déjà.");
        }

        // 2. Création
        Contact contact = contactMapper.toEntity(request);
        contact = contactRepository.save(contact);

        // 3. Log RGPD automatique (Initialisé en PENDING par défaut dans l'entité)
        consentLogService.recordLog(contact, ConsentAction.GRANTED, ipAddress);

        return contactMapper.toResponse(contact);
    }

    @Override
    @Transactional
    public ContactResponse updateContact(UUID trackingId, ContactRequest request) {
        Contact contact = contactRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("Contact introuvable"));

        // On empêche la modification de l'email si un autre compte l'utilise déjà
        if (!contact.getEmail().equals(request.email()) && contactRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà pris par un autre contact.");
        }

        contactMapper.updateEntityFromRequest(request, contact);
        contact = contactRepository.save(contact);

        return contactMapper.toResponse(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse getContactByTrackingId(UUID trackingId) {
        Contact contact = contactRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("Contact introuvable"));
        return contactMapper.toResponse(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> getAllContacts(Pageable pageable) {
        return contactRepository.findAll(pageable)
                .map(contactMapper::toResponse); // Pagination native map
    }

    @Override
    @Transactional
    public ContactResponse changeConsentStatus(UUID trackingId, ConsentStatus newStatus, String ipAddress) {
        Contact contact = contactRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("Contact introuvable"));

        if (contact.getConsentStatus() != newStatus) {
            contact.setConsentStatus(newStatus);
            contact = contactRepository.save(contact);

            ConsentAction action = (newStatus == ConsentStatus.OPT_IN) ? ConsentAction.GRANTED : ConsentAction.REVOKED;
            consentLogService.recordLog(contact, action, ipAddress);
        }

        return contactMapper.toResponse(contact);
    }
}

package tg.univlome.saas.marketing.contact.application.mappers;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.contact.application.dtos.request.ContactRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.domain.models.Contact;

@Component
public class ContactMapper {

    // --- Vers la Réponse (API) ---
    public ContactResponse toResponse(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Le contact ne peut pas être null");
        }

        return new ContactResponse(
                contact.getTrackingId(),
                contact.getEmail(),
                contact.getFirstName(),
                contact.getLastName(),
                contact.getCity(),
                contact.getCountry(),
                contact.getConsentStatus(),
                contact.getCreatedAt()
        );
    }

    public List<ContactResponse> toResponseList(List<Contact> contacts) {
        if (contacts == null) {
            return new ArrayList<>();
        }
        return contacts.stream()
                .map(this::toResponse)
                .toList();
    }

    public Contact toEntity(ContactRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La requête ContactRequest ne peut pas être null");
        }

        Contact contact = new Contact();
        contact.setEmail(request.email());
        contact.setFirstName(request.firstName());
        contact.setLastName(request.lastName());
        contact.setCity(request.city());
        contact.setCountry(request.country());

        // Le trackingId, createdAt et consentStatus(PENDING) sont générés automatiquement par l'entité
        return contact;
    }

    // --- Mise à jour d'une entité existante ---
    public void updateEntityFromRequest(ContactRequest request, Contact contact) {
        if (request == null || contact == null) {
            return;
        }

        contact.setEmail(request.email());
        contact.setFirstName(request.firstName());
        contact.setLastName(request.lastName());
        contact.setCity(request.city());
        contact.setCountry(request.country());
    }
}


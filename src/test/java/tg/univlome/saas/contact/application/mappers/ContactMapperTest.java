package tg.univlome.saas.contact.application.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tg.univlome.saas.marketing.contact.application.dtos.request.ContactRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.application.mappers.ContactMapper;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;
import tg.univlome.saas.marketing.contact.domain.models.Contact;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContactMapperTest {

    private ContactMapper contactMapper;

    @BeforeEach
    void setUp() {
        // ARRANGE global : On instancie le mapper avant chaque test
        contactMapper = new ContactMapper();
    }

    @Test
    void shouldConvertEntityToResponse() {
        // --- 1. ARRANGE ---
        Contact contact = new Contact();
        contact.setId(1L);

        // C'EST ICI qu'on génère et assigne le faux UUID pour le test
        UUID fauxTrackingId = UUID.randomUUID();
        contact.setTrackingId(fauxTrackingId);
        contact.setEmail("test@titan.com");
        contact.setFirstName("Jude");
        contact.setLastName("Worou");
        contact.setCity("Lomé");
        contact.setCountry("Togo");

        // --- 2. ACT ---
        ContactResponse response = contactMapper.toResponse(contact);

        // --- 3. ASSERT ---
        assertNotNull(response);
        assertEquals("test@titan.com", response.email());
        assertEquals("Jude", response.firstName());
        assertEquals("Lomé", response.city());

        // VÉRIFICATION CORRIGÉE : On compare le fauxTrackingId avec l'ID de la réponse
        assertEquals(fauxTrackingId, contact.getTrackingId());
    }

    @Test
    void shouldConvertRequestToEntity() {
        // --- 1. ARRANGE ---
        ContactRequest request = new ContactRequest(
                "dev@titan.com",
                "Dev",
                "Backend",
                "Paris",
                "France",
                ConsentStatus.PENDING,
                LocalDateTime.now()
        );

        // --- 2. ACT ---
        Contact entity = contactMapper.toEntity(request);

        // --- 3. ASSERT ---
        assertNotNull(entity);
        assertEquals("dev@titan.com", entity.getEmail());
        assertEquals("Dev", entity.getFirstName());
        assertEquals("France", entity.getCountry());
        // On vérifie que le statut RGPD par défaut est bien PENDING
        // (Assure-toi que ton ContactMapper initialise bien ça, ou que l'entité le fait par défaut)
        assertEquals(ConsentStatus.PENDING, entity.getConsentStatus());
    }

    @Test
    void shouldUpdateEntityFromRequest() {
        // --- 1. ARRANGE ---
        Contact existingContact = new Contact();
        existingContact.setEmail("old@mail.com");
        existingContact.setCity("Lyon");

        ContactRequest updateRequest = new ContactRequest(
                "new@mail.com",
                "John",
                "Doe",
                "Marseille",
                "France",
                ConsentStatus.PENDING,
                LocalDateTime.now()
        );

        // --- 2. ACT ---
        contactMapper.updateEntityFromRequest(updateRequest, existingContact);

        // --- 3. ASSERT ---
        assertEquals("new@mail.com", existingContact.getEmail());
        assertEquals("John", existingContact.getFirstName());
        assertEquals("Marseille", existingContact.getCity());
    }

    @Test
    void shouldThrowExceptionWhenEntityIsNull() {
        // On vérifie notre sécurité anti-null
        assertThrows(IllegalArgumentException.class, () -> {
            contactMapper.toResponse(null);
        });
    }
}

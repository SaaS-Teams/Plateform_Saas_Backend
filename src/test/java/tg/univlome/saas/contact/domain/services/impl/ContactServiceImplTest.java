package tg.univlome.saas.marketing.contact.domain.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.contact.application.dtos.request.ContactRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.application.mappers.ContactMapper;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.domain.services.ConsentLogService;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Active la magie de Mockito
class ContactServiceImplTest {

    // --- LES "FAUX" OUTILS (MOCKS) ---
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ContactMapper contactMapper;
    @Mock
    private ConsentLogService consentLogService;

    // --- LE VRAI SERVICE À TESTER ---
    @InjectMocks
    private ContactServiceImpl contactService;

    @Test
    void shouldCreateContactSuccessfully() {
        // --- 1. ARRANGE ---
        ContactRequest request = new ContactRequest(
                "test@mail.com",
                "John",
                "Doe",
                "Paris",
                "FR",
                ConsentStatus.PENDING,
                LocalDateTime.now());
        Contact mockContact = new Contact();
        mockContact.setEmail("test@mail.com");

        ContactResponse expectedResponse = new ContactResponse(UUID.randomUUID(), "test@mail.com", "John", "Doe", "Paris", "FR", ConsentStatus.PENDING, null);

        // On dicte le comportement des Mocks
        // 1. Quand on cherche l'email, on dit qu'il n'existe pas encore (Optional.empty)
        when(contactRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());
        // 2. Quand on demande de mapper la requête en entité, on renvoie notre mockContact
        when(contactMapper.toEntity(request)).thenReturn(mockContact);
        // 3. Quand on sauvegarde, on renvoie l'entité sauvegardée
        when(contactRepository.save(any(Contact.class))).thenReturn(mockContact);
        // 4. Quand on map l'entité vers la réponse, on renvoie expectedResponse
        when(contactMapper.toResponse(mockContact)).thenReturn(expectedResponse);

        // --- 2. ACT ---
        ContactResponse result = contactService.createContact(request, "192.168.1.1");

        // --- 3. ASSERT ---
        assertNotNull(result);
        assertEquals("test@mail.com", result.email());

        // VÉRIFICATION CRUCIALE : On vérifie que le log RGPD a bien été enregistré !
        verify(consentLogService, times(1)).recordLog(mockContact, ConsentAction.GRANTED, "192.168.1.1");
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExistsOnCreation() {
        // --- 1. ARRANGE ---
        ContactRequest request = new ContactRequest(
                "doublon@mail.com",
                "John",
                "Doe",
                "Paris",
                "FR",
                ConsentStatus.PENDING,
                LocalDateTime.now());
        Contact existingContact = new Contact();

        // On dicte au Mock que l'email est déjà pris !
        when(contactRepository.findByEmail("doublon@mail.com")).thenReturn(Optional.of(existingContact));

        // --- 2. ACT & ASSERT ---
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            contactService.createContact(request, "192.168.1.1");
        });

        assertEquals("Un contact avec cet email existe déjà.", exception.getMessage());

        // On s'assure que la sauvegarde n'a JAMAIS été appelée
        verify(contactRepository, never()).save(any());
        verify(consentLogService, never()).recordLog(any(), any(), any());
    }

    @Test
    void shouldChangeConsentStatusAndRecordLog() {
        // --- 1. ARRANGE ---
        UUID contactId = UUID.randomUUID();
        Contact existingContact = new Contact();
        existingContact.setConsentStatus(ConsentStatus.PENDING); // Ancien statut

        when(contactRepository.findByTrackingId(contactId)).thenReturn(Optional.of(existingContact));
        when(contactRepository.save(any(Contact.class))).thenReturn(existingContact);
        // On triche un peu sur la réponse pour le test
        when(contactMapper.toResponse(existingContact)).thenReturn(new ContactResponse(contactId, "test@mail.com", null, null, null, null, ConsentStatus.OPT_IN, null));

        // --- 2. ACT ---
        // On simule que l'utilisateur accepte les emails (OPT_IN)
        ContactResponse result = contactService.changeConsentStatus(contactId, ConsentStatus.OPT_IN, "127.0.0.1");

        // --- 3. ASSERT ---
        assertEquals(ConsentStatus.OPT_IN, existingContact.getConsentStatus());

        // On vérifie que l'action GRANTED a bien été loggée pour la CNIL
        verify(consentLogService, times(1)).recordLog(existingContact, ConsentAction.GRANTED, "127.0.0.1");
    }
}

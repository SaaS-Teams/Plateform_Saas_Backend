package tg.univlome.saas.marketing.contact.domain.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ConsentLogResponse;
import tg.univlome.saas.marketing.contact.application.mappers.ConsentLogMapper;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;
import tg.univlome.saas.marketing.contact.domain.models.ConsentLog;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.repositories.ConsentLogRepository;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ConsentLogServiceImplTest {

    @Mock
    private ConsentLogRepository consentLogRepository;
    @Mock private ContactRepository contactRepository;
    @Mock private ConsentLogMapper consentLogMapper;

    @InjectMocks
    private ConsentLogServiceImpl consentLogService;

    @Test
    void shouldRecordLog() {
        // --- ARRANGE ---
        Contact mockContact = new Contact();

        // --- ACT ---
        consentLogService.recordLog(mockContact, ConsentAction.GRANTED, "192.168.1.1");

        // --- ASSERT ---
        // On vérifie simplement que l'ordre de sauvegarde a été envoyé à la base
        verify(consentLogRepository, times(1)).save(any(ConsentLog.class));
    }

    @Test
    void shouldGetContactConsentHistory() {
        // --- ARRANGE ---
        UUID contactId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(0, 10);

        // Simule que le contact existe
        when(contactRepository.findByTrackingId(contactId)).thenReturn(Optional.of(new Contact()));

        // Simule les logs trouvés en base
        ConsentLog log = new ConsentLog(new Contact(), ConsentAction.GRANTED, "ip");
        Page<ConsentLog> mockPage = new PageImpl<>(List.of(log));

        when(consentLogRepository.findByContact_TrackingId(contactId, pageRequest)).thenReturn(mockPage);
        when(consentLogMapper.toResponse(log)).thenReturn(new ConsentLogResponse(contactId, UUID.randomUUID(), ConsentAction.GRANTED, "ip", null));

        // --- ACT ---
        Page<ConsentLogResponse> result = consentLogService.getContactConsentHistory(contactId, pageRequest);

        // --- ASSERT ---
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(ConsentAction.GRANTED, result.getContent().get(0).action());
    }

    @Test
    void shouldThrowExceptionIfContactNotFoundForHistory() {
        // --- ARRANGE ---
        UUID unknownContactId = UUID.randomUUID();
        // On dit à la base qu'elle ne trouve rien
        when(contactRepository.findByTrackingId(unknownContactId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThrows(IllegalArgumentException.class, () -> {
            consentLogService.getContactConsentHistory(unknownContactId, PageRequest.of(0, 10));
        });

        // On vérifie qu'il n'a pas essayé de chercher les logs d'un contact inexistant
        verify(consentLogRepository, never()).findByContact_TrackingId(any(), any());
    }
}

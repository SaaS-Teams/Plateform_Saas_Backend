package tg.univlome.saas.marketing.contact.domain.services.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tg.univlome.saas.marketing.contact.application.dtos.request.SegmentRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.application.dtos.response.SegmentResponse;
import tg.univlome.saas.marketing.contact.application.mappers.ContactMapper;
import tg.univlome.saas.marketing.contact.application.mappers.SegmentMapper;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.domain.models.ContactSegment;
import tg.univlome.saas.marketing.contact.domain.models.Segment;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;
import tg.univlome.saas.marketing.contact.repositories.ContactSegmentRepository;
import tg.univlome.saas.marketing.contact.repositories.SegmentRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class SegmentServiceImplTest {

    @Mock private SegmentRepository segmentRepository;
    @Mock private ContactRepository contactRepository;
    @Mock private ContactSegmentRepository contactSegmentRepository;
    @Mock private SegmentMapper segmentMapper;
    @Mock private ContactMapper contactMapper;

    @InjectMocks
    private SegmentServiceImpl segmentService;

    @Test
    void shouldCreateSegmentSuccessfully() {
        // --- ARRANGE ---
        SegmentRequest request = new SegmentRequest("VIP", "Client VIP", "{}");
        Segment mockSegment = new Segment();
        mockSegment.setName("VIP");
        SegmentResponse expectedResponse = new SegmentResponse(UUID.randomUUID(), "VIP", "Client VIP", "{}", null);

        // On vérifie que le nom n'est pas pris
        when(segmentRepository.findByName("VIP")).thenReturn(Optional.empty());
        when(segmentMapper.toEntity(request)).thenReturn(mockSegment);
        when(segmentRepository.save(any(Segment.class))).thenReturn(mockSegment);
        when(segmentMapper.toResponse(mockSegment)).thenReturn(expectedResponse);

        // --- ACT ---
        SegmentResponse result = segmentService.createSegment(request);

        // --- ASSERT ---
        assertNotNull(result);
        assertEquals("VIP", result.name());
        verify(segmentRepository, times(1)).save(any(Segment.class));
    }

    @Test
    void shouldAddContactToSegment() {
        // --- ARRANGE ---
        UUID contactId = UUID.randomUUID();
        UUID segmentId = UUID.randomUUID();
        Contact mockContact = new Contact();
        Segment mockSegment = new Segment();

        // 1. On simule que la liaison n'existe pas encore
        when(contactSegmentRepository.existsByContact_TrackingIdAndSegment_TrackingId(contactId, segmentId)).thenReturn(false);
        // 2. On simule qu'on trouve bien le contact et le segment en base
        when(contactRepository.findByTrackingId(contactId)).thenReturn(Optional.of(mockContact));
        when(segmentRepository.findByTrackingId(segmentId)).thenReturn(Optional.of(mockSegment));

        // --- ACT ---
        segmentService.addContactToSegment(contactId, segmentId);

        // --- ASSERT ---
        // On vérifie que la sauvegarde dans la table de liaison a bien été appelée
        verify(contactSegmentRepository, times(1)).save(any(ContactSegment.class));
    }

    @Test
    void shouldGetContactsBySegmentWithPagination() {
        // --- ARRANGE ---
        UUID segmentId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(0, 10);

        // On simule un segment existant
        when(segmentRepository.findByTrackingId(segmentId)).thenReturn(Optional.of(new Segment()));

        // On simule une page de résultats venant de la base de données (1 liaison)
        Contact mockContact = new Contact();
        ContactSegment liaison = new ContactSegment(mockContact, new Segment());
        Page<ContactSegment> mockPage = new PageImpl<>(List.of(liaison)); // PageImpl crée une fausse "Page"

        when(contactSegmentRepository.findBySegment_TrackingId(segmentId, pageRequest)).thenReturn(mockPage);
        when(contactMapper.toResponse(mockContact)).thenReturn(new ContactResponse(null, "mail", null, null, null, null, null, null));

        // --- ACT ---
        Page<ContactResponse> result = segmentService.getContactsBySegment(segmentId, pageRequest);

        // --- ASSERT ---
        assertNotNull(result);
        assertEquals(1, result.getTotalElements()); // On vérifie qu'il y a bien 1 élément dans la page
    }
}

package tg.univlome.saas.marketing.contact.domain.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.univlome.saas.marketing.contact.application.dtos.request.SegmentRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.application.dtos.response.SegmentResponse;

import java.util.UUID;

public interface SegmentService {
    SegmentResponse createSegment(SegmentRequest request);
    SegmentResponse getSegmentByTrackingId(UUID trackingId);
    Page<SegmentResponse> getAllSegments(Pageable pageable);

    // --- Gestion de la table de liaison ContactSegment ---
    void addContactToSegment(UUID contactTrackingId, UUID segmentTrackingId);
    void removeContactFromSegment(UUID contactTrackingId, UUID segmentTrackingId);

    // Pagination : Récupère tous les contacts appartenant à un segment
    Page<ContactResponse> getContactsBySegment(UUID segmentTrackingId, Pageable pageable);
}

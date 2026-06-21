package tg.univlome.saas.marketing.contact.domain.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.contact.application.dtos.request.SegmentRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.application.dtos.response.SegmentResponse;
import tg.univlome.saas.marketing.contact.application.mappers.ContactMapper;
import tg.univlome.saas.marketing.contact.application.mappers.SegmentMapper;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.domain.models.ContactSegment;
import tg.univlome.saas.marketing.contact.domain.models.Segment;
import tg.univlome.saas.marketing.contact.domain.services.SegmentService;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;
import tg.univlome.saas.marketing.contact.repositories.ContactSegmentRepository;
import tg.univlome.saas.marketing.contact.repositories.SegmentRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SegmentServiceImpl implements SegmentService {

    private final SegmentRepository segmentRepository;
    private final ContactRepository contactRepository;
    private final ContactSegmentRepository contactSegmentRepository;
    private final SegmentMapper segmentMapper;
    private final ContactMapper contactMapper;

    @Override
    @Transactional
    public SegmentResponse createSegment(SegmentRequest request) {
        if (segmentRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("Un segment avec ce nom existe déjà.");
        }

        Segment segment = segmentMapper.toEntity(request);
        segment = segmentRepository.save(segment);
        return segmentMapper.toResponse(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public SegmentResponse getSegmentByTrackingId(UUID trackingId) {
        Segment segment = segmentRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("Segment introuvable"));
        return segmentMapper.toResponse(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SegmentResponse> getAllSegments(Pageable pageable) {
        return segmentRepository.findAll(pageable).map(segmentMapper::toResponse);
    }

    // --- LOGIQUE DE LIAISON (Contact <-> Segment) ---

    @Override
    @Transactional
    public void addContactToSegment(UUID contactTrackingId, UUID segmentTrackingId) {
        if (contactSegmentRepository.existsByContact_TrackingIdAndSegment_TrackingId(contactTrackingId, segmentTrackingId)) {
            return; // Le contact est déjà dans le segment, on ne fait rien
        }

        Contact contact = contactRepository.findByTrackingId(contactTrackingId)
                .orElseThrow(() -> new IllegalArgumentException("Contact introuvable"));
        Segment segment = segmentRepository.findByTrackingId(segmentTrackingId)
                .orElseThrow(() -> new IllegalArgumentException("Segment introuvable"));

        ContactSegment liaison = new ContactSegment();
        liaison.setSegment(segment);
        liaison.setContact(contact);
        contactSegmentRepository.save(liaison);
    }

    @Override
    @Transactional
    public void removeContactFromSegment(UUID contactTrackingId, UUID segmentTrackingId) {
        contactSegmentRepository.deleteByContact_TrackingIdAndSegment_TrackingId(contactTrackingId, segmentTrackingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> getContactsBySegment(UUID segmentTrackingId, Pageable pageable) {
        // On vérifie que le segment existe
        if (!segmentRepository.findByTrackingId(segmentTrackingId).isPresent()) {
            throw new IllegalArgumentException("Segment introuvable");
        }

        // On récupère la table de liaison, on extrait les contacts, on map en DTO avec pagination !
        Page<ContactSegment> liaisons = contactSegmentRepository.findBySegment_TrackingId(segmentTrackingId, pageable);
        return liaisons.map(liaison -> contactMapper.toResponse(liaison.getContact()));
    }
}
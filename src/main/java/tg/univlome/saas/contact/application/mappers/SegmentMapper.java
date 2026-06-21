package tg.univlome.saas.marketing.contact.application.mappers;

import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.contact.application.dtos.request.SegmentRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.SegmentResponse;
import tg.univlome.saas.marketing.contact.domain.models.Segment;

import java.util.ArrayList;
import java.util.List;

@Component
public class SegmentMapper {

    public SegmentResponse toResponse(Segment segment) {
        if (segment == null) {
            throw new IllegalArgumentException("Le segment ne peut pas être null");
        }

        return new SegmentResponse(
                segment.getTrackingId(),
                segment.getName(),
                segment.getDescription(),
                segment.getRulesJson(),
                segment.getCreatedAt()
        );
    }

    public List<SegmentResponse> toResponseList(List<Segment> segments) {
        if (segments == null) {
            return new ArrayList<>();
        }
        return segments.stream()
                .map(this::toResponse)
                .toList();
    }

    public Segment toEntity(SegmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La requête SegmentRequest ne peut pas être null");
        }

        Segment segment = new Segment();
        segment.setName(request.name());
        segment.setDescription(request.description());
        segment.setRulesJson(request.rulesJson());

        return segment;
    }

    public void updateEntityFromRequest(SegmentRequest request, Segment segment) {
        if (request == null || segment == null) {
            return;
        }

        segment.setName(request.name());
        segment.setDescription(request.description());
        segment.setRulesJson(request.rulesJson());
    }
}

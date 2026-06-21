package tg.univlome.saas.marketing.contact.application.dtos.response;

import java.util.UUID;

public record ContactSegmentResponse(
        UUID contactSegmentTrackingId,
        UUID contactTrackingId,
        UUID segmentTrackingId
) {
}

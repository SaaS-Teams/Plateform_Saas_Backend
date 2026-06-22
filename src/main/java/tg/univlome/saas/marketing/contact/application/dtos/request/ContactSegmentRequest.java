package tg.univlome.saas.marketing.contact.application.dtos.request;

import java.util.UUID;

public record ContactSegmentRequest(
        UUID contactTrackingId,
        UUID segmentTrackingId
) {

}


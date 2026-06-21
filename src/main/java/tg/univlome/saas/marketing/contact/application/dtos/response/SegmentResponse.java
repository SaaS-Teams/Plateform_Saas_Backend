package tg.univlome.saas.marketing.contact.application.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SegmentResponse(
        UUID trackingId,           // Mappé depuis le trackingId de l'entité
        String name,
        String description,
        String rulesJson,
        LocalDateTime createdAt
) {
}


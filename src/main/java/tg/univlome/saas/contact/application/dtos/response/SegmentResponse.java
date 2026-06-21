package tg.univlome.saas.marketing.contact.application.dtos.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SegmentResponse(
        UUID trackingId,           // Mappé depuis le trackingId de l'entité
        String name,
        String description,
        String rulesJson,
        LocalDateTime createdAt
) {}

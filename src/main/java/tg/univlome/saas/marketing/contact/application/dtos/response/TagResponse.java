package tg.univlome.saas.marketing.contact.application.dtos.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record TagResponse(
        UUID trackingId,
        String name,
        String color
) {
}

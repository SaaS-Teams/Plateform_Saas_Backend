package tg.univlome.saas.marketing.contact.application.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;

@Builder
public record ContactResponse(
        UUID trackingId,           // Provient de contact.getTrackingId()
        String email,
        String firstName,
        String lastName,
        String city,
        String country,
        ConsentStatus consentStatus,
        LocalDateTime createdAt
) {
}

package tg.univlome.saas.marketing.contact.application.dtos.request;

import java.time.LocalDateTime;
import lombok.Builder;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;

@Builder
public record ContactRequest(
        String email,
        String firstName,
        String lastName,
        String city,
        String country,
        ConsentStatus consentStatus,
        LocalDateTime createdAt
) {

}

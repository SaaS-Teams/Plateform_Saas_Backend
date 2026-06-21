package tg.univlome.saas.marketing.contact.application.dtos.request;

import lombok.Builder;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;

import java.time.LocalDateTime;

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
package tg.univlome.saas.marketing.contact.application.dtos.request;

import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;

import java.util.UUID;

public record ConsentLogRequest(
        UUID contactTracignId,
        ConsentAction action,
        String ipAddress
) {
}


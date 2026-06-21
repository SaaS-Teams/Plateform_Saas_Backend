package tg.univlome.saas.marketing.contact.application.dtos.request;

import java.util.UUID;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;

public record ConsentLogRequest(
        UUID contactTracignId,
        ConsentAction action,
        String ipAddress
) {
}


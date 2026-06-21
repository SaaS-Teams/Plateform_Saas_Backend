package tg.univlome.saas.marketing.contact.application.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;

public record ConsentLogResponse(

        UUID contactTracignId,
        UUID trackingId,
        ConsentAction action,
        String ipAddress,
        LocalDateTime createAt
) {
}


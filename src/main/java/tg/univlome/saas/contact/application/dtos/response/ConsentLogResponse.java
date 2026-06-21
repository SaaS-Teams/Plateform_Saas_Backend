package tg.univlome.saas.marketing.contact.application.dtos.response;

import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsentLogResponse (

    UUID contactTracignId,
    UUID trackingId,
    ConsentAction action,
    String ipAddress,
    LocalDateTime createAt
) {}

package tg.univlome.saas.marketing.contact.application.dtos.request;

import java.util.List;
import java.util.UUID;

public record ContactFilterRequest(
        String search,
        List<UUID> tagTrackingIds
) {
}

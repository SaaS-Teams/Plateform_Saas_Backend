package tg.univlome.saas.marketing.campagne.application.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;
import tg.univlome.saas.marketing.campagne.domain.enums.CampagneStatus;

public record CampagneResponse(
        Long id,
        UUID trackingId,
        String nom,
        String sujet,
        String contenu,
        CampagneStatus statut,
        LocalDateTime datePlanification,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

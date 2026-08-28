package tg.univlome.saas.web.dtos.auth;

import java.util.UUID;

/**
 * DTO de réponse contenant les informations de l'utilisateur connecté (/me).
 */
public record UserResponse(
        UUID onboardingUuid,
        String email,
        String firstName,
        String lastName,
        Boolean onboardingCompleted
) {
}

package tg.univlome.saas.web.dtos.auth;

import java.util.UUID;

/**
 * DTO de réponse d'authentification incluant le token JWT et les informations d'onboarding.
 */
public record AuthResponse(
        String token,
        String type,
        UUID onboardingUuid,
        String email,
        String firstName,
        String lastName,
        Boolean onboardingCompleted
) {
    public AuthResponse(String token, UUID onboardingUuid, String email, String firstName, String lastName, Boolean onboardingCompleted) {
        this(token, "Bearer", onboardingUuid, email, firstName, lastName, onboardingCompleted);
    }
}

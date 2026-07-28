package tg.univlome.saas.web.dtos.mobile;

import java.util.UUID;

/**
 * DTO de réponse pour l'état d'avancement de l'onboarding mobile.
 */
public record OnboardingResponse(
        UUID onboardingUuid,
        Boolean isCompleted
) {
}

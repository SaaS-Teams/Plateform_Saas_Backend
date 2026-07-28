package tg.univlome.saas.web.dtos.mobile;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour l'étape 2 du tunnel d'onboarding mobile (nom et prénom).
 */
public record OnboardingStep2Request(
        @NotBlank(message = "Le prénom est obligatoire")
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        String lastName
) {
}

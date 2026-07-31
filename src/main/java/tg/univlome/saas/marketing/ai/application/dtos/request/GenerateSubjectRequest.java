package tg.univlome.saas.marketing.ai.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record GenerateSubjectRequest(
        @NotBlank(message = "Le sujet ou contenu principal de l'email est obligatoire")
        String emailContent,

        String tone,

        String language
) {
}

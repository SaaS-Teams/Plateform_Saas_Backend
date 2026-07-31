package tg.univlome.saas.marketing.ai.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record GenerateEmailRequest(
        @NotBlank(message = "Le sujet ou thème de l'email est obligatoire")
        String topic,

        String tone,

        String targetAudience,

        String language
) {
}

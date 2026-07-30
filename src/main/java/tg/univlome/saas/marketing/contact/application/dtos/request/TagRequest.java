package tg.univlome.saas.marketing.contact.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank(message = "Le nom du tag est obligatoire")
        String name,

        String color
) {
}

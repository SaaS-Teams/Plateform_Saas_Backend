package tg.univlome.saas.email.application.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailMessage(
        @NotBlank(message = "L'adresse email du destinataire est requise")
        @Email(message = "L'adresse email doit être valide")
        String to,

        @NotBlank(message = "Le sujet de l'email est requis")
        String subject,

        @NotBlank(message = "Le corps de l'email est requis")
        String body,

        boolean isHtml
) { }

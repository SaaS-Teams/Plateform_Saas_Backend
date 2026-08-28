package tg.univlome.saas.web.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requête d'authentification.
 */
public record LoginRequest(
        @NotBlank(message = "L'adresse email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String password
) {
}

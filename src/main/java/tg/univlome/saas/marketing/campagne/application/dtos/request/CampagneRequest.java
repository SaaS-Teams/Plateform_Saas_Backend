package tg.univlome.saas.marketing.campagne.application.dtos.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;


public record CampagneRequest(
        @NotBlank(message = "Le nom de la campagne est obligatoire")
        String nom,

        @NotBlank(message = "Le sujet de l'email est obligatoire")
        String sujet,

        @NotBlank(message = "Le contenu du message est obligatoire")
        String contenu,

        // On s'assure que si une date est fournie, elle est dans le futur
        @Future(message = "La date de planification doit être dans le futur")
        LocalDateTime datePlanification
) {
}

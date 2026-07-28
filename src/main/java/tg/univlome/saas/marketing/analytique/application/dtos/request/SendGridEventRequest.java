package tg.univlome.saas.marketing.analytique.application.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record SendGridEventRequest(
        String email,

        // Le type d'événement chez SendGrid ("delivered", "open", "click", "bounce", "spamreport")
        String event,

        // SendGrid envoie un Timestamp Unix (en secondes)
        Long timestamp,

        // Métadonnées
        String ip,
        String useragent,

        // Présent uniquement si l'événement est un "click"
        String url,

        // --- LE PASSEPORT ---
        // L'annotation indique à Jackson de lire le champ "tracking_id" du JSON
        // et de le mettre dans cette variable
        @JsonProperty("tracking_id")
        UUID trackingId
) {}

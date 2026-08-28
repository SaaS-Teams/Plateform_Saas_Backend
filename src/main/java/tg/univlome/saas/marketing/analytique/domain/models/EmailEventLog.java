package tg.univlome.saas.marketing.analytique.domain.models;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tg.univlome.saas.marketing.analytique.domain.enums.EmailEventType;

// @Document est l'équivalent NoSQL de @Entity
@Document(collection = "email_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailEventLog {

    @Id // Import issu de org.springframework.data.annotation.Id
    private String id; // L'identifiant généré par MongoDB (format alphanumérique)

    // La clé de voûte (référence douce) pour lier cet événement
    // à l'exécution du workflow stockée dans PostgreSQL
    private UUID trackingId;

    private String contactEmail;

    private EmailEventType eventType;

    // Utile uniquement si l'événement est de type CLICKED
    private String clickedUrl;

    private LocalDateTime timestamp;

    // Les métadonnées pour l'analyse (ex: "Chrome sur Mac", "192.168.1.1")
    private String userAgent;
    private String ipAddress;
}

package tg.univlome.saas.marketing.analytique.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.marketing.analytique.domain.enums.EmailEventType;
import tg.univlome.saas.marketing.analytique.domain.models.EmailEventLog;

@Repository
public interface EmailEventLogRepository extends MongoRepository<EmailEventLog, String> {

    // Récupérer tout l'historique d'une exécution de workflow
    List<EmailEventLog> findByTrackingId(UUID trackingId);

    // Filtrer par événement (ex: récupérer tous les "CLICKED" pour voir les liens cliqués)
    List<EmailEventLog> findByTrackingIdAndEventType(UUID trackingId, EmailEventType eventType);

    // Méthode clé pour le Dashboard : Compter rapidement sans charger les données
    long countByTrackingIdAndEventType(UUID trackingId, EmailEventType eventType);
}

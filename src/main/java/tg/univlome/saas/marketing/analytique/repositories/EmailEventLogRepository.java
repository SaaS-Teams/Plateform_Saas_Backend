package tg.univlome.saas.marketing.analytique.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.marketing.analytique.domain.enums.EmailEventType;
import tg.univlome.saas.marketing.analytique.domain.models.EmailEventLog;
import tg.univlome.saas.marketing.analytique.repositories.projections.DailyStatsProjection;

@Repository
public interface EmailEventLogRepository extends MongoRepository<EmailEventLog, String> {

    // Compter les événements globaux par type d'événement
    long countByEventType(EmailEventType eventType);

    // Récupérer tout l'historique d'une exécution de workflow
    List<EmailEventLog> findByTrackingId(UUID trackingId);

    // Filtrer par événement
    List<EmailEventLog> findByTrackingIdAndEventType(UUID trackingId, EmailEventType eventType);

    // Compter rapidement par trackingId et eventType
    long countByTrackingIdAndEventType(UUID trackingId, EmailEventType eventType);

    // Extraction des statistiques par jour (Time-series pour les graphiques)
    @Aggregation(pipeline = {
        "{ '$project': { 'date': { '$dateToString': { 'format': '%Y-%m-%d', 'date': '$timestamp' } }, 'eventType': 1 } }",
        "{ '$group': { '_id': '$date', 'opens': { '$sum': { '$cond': [ { '$eq': ['$eventType', 'OPENED'] }, 1, 0 ] } }, "
                + "'clicks': { '$sum': { '$cond': [ { '$eq': ['$eventType', 'CLICKED'] }, 1, 0 ] } } } }",
        "{ '$project': { 'date': '$_id', 'opens': '$opens', 'clicks': '$clicks' } }",
        "{ '$sort': { 'date': 1 } }"
    })
    List<DailyStatsProjection> findDailyStatsGroupedByDate();
}

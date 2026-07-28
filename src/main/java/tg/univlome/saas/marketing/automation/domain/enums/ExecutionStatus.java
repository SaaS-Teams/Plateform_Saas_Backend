package tg.univlome.saas.marketing.automation.domain.enums;

public enum ExecutionStatus {
    PENDING,      // En attente de démarrage (placé dans RabbitMQ)
    IN_PROGRESS,  // Actuellement en cours (traité par les workers et suivi dans Redis)
    COMPLETED,    // Parcours terminé avec succès
    FAILED,       // Arrêté suite à une erreur (ex: email invalide)
    CANCELLED     // Annulé manuellement ou par une règle métier
}

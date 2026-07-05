package tg.univlome.saas.marketing.campagne.domain.enums;


public enum CampagneStatus {
    BROUILLON,      // La campagne est en cours de rédaction
    PROGRAMMEE,     // La campagne est prête et attend sa date de planification
    EN_COURS,       // Le système est en train d'envoyer les emails
    TERMINEE,       // Tous les emails ont été envoyés
    ANNULEE         // La campagne a été stoppée par l'utilisateur
}

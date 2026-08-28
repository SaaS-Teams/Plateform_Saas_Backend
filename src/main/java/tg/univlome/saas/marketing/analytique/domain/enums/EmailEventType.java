package tg.univlome.saas.marketing.analytique.domain.enums;

public enum EmailEventType {
    DELIVERED,      // L'e-mail est bien arrivé dans la boîte de réception
    OPENED,         // L'utilisateur a ouvert l'e-mail
    CLICKED,        // L'utilisateur a cliqué sur un lien dedans
    BOUNCED,        // L'e-mail a été rejeté (fausse adresse, boîte pleine)
    SPAM_REPORT     // L'utilisateur a cliqué sur "Signaler comme Spam"
}

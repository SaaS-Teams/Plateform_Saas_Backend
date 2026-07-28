package tg.univlome.saas.marketing.automation.domain.services;

public interface RateLimitingService {
    /**
     * Vérifie si l'utilisateur a le droit d'envoyer un message maintenant.
     * @return true si autorisé (Feu vert), false s'il faut remettre en file d'attente (Feu rouge)
     */
    boolean isAllowed(String accountId, int maxPerMinute);
}

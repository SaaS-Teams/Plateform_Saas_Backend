package tg.univlome.saas.marketing.automation.application.handlers;

import java.util.Map;

/**
 * Contrat du pattern Strategy pour le traitement spécifique des payloads de webhooks entrants.
 */
public interface WebhookPayloadHandler {

    /**
     * Indique si cette stratégie supporte le fournisseur spécifié.
     *
     * @param provider le nom du fournisseur (ex: "sendgrid", "twilio")
     * @return true si ce handler peut traiter le payload, false sinon
     */
    boolean supports(String provider);

    /**
     * Traite le contenu du payload de l'événement webhook entrant.
     *
     * @param payload les données de l'événement
     */
    void process(Map<String, Object> payload);
}

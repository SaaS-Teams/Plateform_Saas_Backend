package tg.univlome.saas.marketing.automation.domain.services;

import java.util.Map;

/**
 * Contrat du service de déclenchement de webhooks dans le moteur d'automation.
 *
 * <p>Permet d'appeler n'importe quelle URL externe (Zapier, Make, système interne)
 * avec un payload JSON lors de l'exécution d'un nœud {@code WEBHOOK} dans un scénario.</p>
 *
 * <p>Les implémentations futures utiliseront {@code WebClient} ou {@code RestTemplate}
 * pour effectuer la requête HTTP réelle.</p>
 */
public interface WebhookService {

    /**
     * Déclenche une requête HTTP POST vers une URL externe avec le payload fourni.
     *
     * @param url     l'URL cible du webhook (ex: {@code https://hooks.zapier.com/hooks/catch/xxx})
     * @param payload le corps de la requête, sérialisé en JSON par l'implémentation
     */
    void triggerWebhook(String url, Map<String, Object> payload);
}

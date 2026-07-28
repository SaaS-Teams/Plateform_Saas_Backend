package tg.univlome.saas.marketing.automation.domain.services.impl;

import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tg.univlome.saas.marketing.automation.domain.services.WebhookService;

/**
 * Implémentation réelle du service Webhook basée sur {@link WebClient}.
 *
 * <p>Justification du choix de {@code WebClient} :</p>
 * <ul>
 *   <li>{@code RestTemplate} est bloquant et déprécié/en maintenance synchrone dans Spring 5+.</li>
 *   <li>{@code WebClient} (issu de {@code spring-boot-starter-webflux}) permet d'exécuter des appels
 *       HTTP réactifs non bloquants avec une gestion fine des timeouts et des codes d'erreur HTTP.</li>
 * </ul>
 *
 * <p>Gestion de la résilience :</p>
 * <ul>
 *   <li>Mode "fire-and-forget" non bloquant pour le worker RabbitMQ.</li>
 *   <li>Timeout de connexion et de réponse réglé à 5 secondes.</li>
 *   <li>Les erreurs HTTP 4xx, 5xx ou les interruptions réseau sont capturées et logguées en LEVEL ERROR
 *       sans jamais lever d'exception remontant au moteur d'exécution.</li>
 * </ul>
 */
@Slf4j
@Service
public class WebhookServiceImpl implements WebhookService {

    private final WebClient webClient;

    public WebhookServiceImpl() {
        this.webClient = WebClient.builder().build();
    }

    @Override
    public void triggerWebhook(String url, Map<String, Object> payload) {
        if (url == null || url.isBlank()) {
            log.error("[WEBHOOK SERVICE] Impossible d'exécuter le webhook : l'URL cible est vide ou null.");
            return;
        }

        log.info("[WEBHOOK SERVICE] Envoi de la requête HTTP POST vers [{}]", url);

        try {
            this.webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(payload != null ? payload : Map.of())
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(5))
                    .doOnSuccess(response -> log.info(
                            "[WEBHOOK SERVICE] Webhook déclenché avec succès vers [{}] — Status Code: {}",
                            url, response.getStatusCode()))
                    .doOnError(error -> log.error(
                            "[WEBHOOK SERVICE] Échec de l'appel Webhook vers [{}] — Cause: {}",
                            url, error.getMessage()))
                    .subscribe(); // Exécution réactive asynchrone non bloquante (Fire and Forget)

        } catch (Exception e) {
            // Sécurité globale pour capturer toute exception synchrone éventuelle lors de la construction
            log.error("[WEBHOOK SERVICE] Erreur inattendue lors de la préparation du Webhook vers [{}] : {}",
                    url, e.getMessage(), e);
        }
    }
}

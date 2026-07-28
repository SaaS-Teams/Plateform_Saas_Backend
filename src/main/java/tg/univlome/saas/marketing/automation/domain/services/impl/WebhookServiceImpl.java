package tg.univlome.saas.marketing.automation.domain.services.impl;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.automation.domain.services.WebhookService;

/**
 * Implémentation de démonstration du service Webhook.
 *
 * <p>Log le déclenchement de webhooks HTTP en attendant l'intégration avec WebClient/RestTemplate.</p>
 */
@Slf4j
@Service
public class WebhookServiceImpl implements WebhookService {

    @Override
    public void triggerWebhook(String url, Map<String, Object> payload) {
        log.info("[WEBHOOK SERVICE] Appel HTTP POST vers [{}] — Payload : {}", url, payload);
    }
}

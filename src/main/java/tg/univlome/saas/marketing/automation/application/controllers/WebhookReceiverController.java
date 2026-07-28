package tg.univlome.saas.marketing.automation.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.automation.domain.services.impl.InboundEventProducer;

/**
 * Contrôleur d'ingestion ultra-rapide des webhooks entrants (SendGrid, Twilio, etc.).
 *
 * <p>Reçoit le payload et le dépose immédiatement dans la file RabbitMQ
 * avant de répondre avec un statut HTTP 202 Accepted, garantissant un temps
 * de réponse minimal et évitant les timeouts des fournisseurs tiers.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Inbound Webhooks", description = "Endpoints publics de réception des événements webhooks entrants")
public class WebhookReceiverController {

    private final InboundEventProducer inboundEventProducer;

    @PostMapping("/{provider}")
    @Operation(summary = "Réception et ingestion asynchrone d'un webhook entrant")
    public ResponseEntity<Void> receiveWebhook(
            @PathVariable("provider") String provider,
            @RequestBody Map<String, Object> payload) {

        log.info("[WEBHOOK RECEIVER] Webhook entrant reçu pour le provider [{}]", provider);

        // Publication asynchrone ultra-rapide dans RabbitMQ
        inboundEventProducer.publishEvent(provider, payload);

        // Retour immédiat HTTP 202 Accepted
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}

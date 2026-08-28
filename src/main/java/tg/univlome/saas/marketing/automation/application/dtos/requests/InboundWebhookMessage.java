package tg.univlome.saas.marketing.automation.application.dtos.requests;

import java.io.Serializable;
import java.util.Map;

/**
 * Message d'événement webhook entrant publié dans RabbitMQ pour traitement asynchrone.
 *
 * @param provider le fournisseur de provenance du webhook (ex: "sendgrid", "twilio", "stripe")
 * @param payload  les données transmises dans le corps du webhook
 */
public record InboundWebhookMessage(
        String provider,
        Map<String, Object> payload
) implements Serializable {
}

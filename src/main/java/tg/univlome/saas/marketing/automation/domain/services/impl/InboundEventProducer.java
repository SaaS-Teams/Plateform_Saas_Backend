package tg.univlome.saas.marketing.automation.domain.services.impl;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.automation.application.dtos.requests.InboundWebhookMessage;

/**
 * Service Producer responsable de l'envoi rapide des webhooks entrants dans la file RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${saas.rabbitmq.exchange.inbound:inbound-events-exchange}")
    private String inboundExchange;

    @Value("${saas.rabbitmq.routing-key.inbound:inbound.event.receive}")
    private String inboundRoutingKey;

    /**
     * Publie un événement de webhook entrant dans l'exchange RabbitMQ.
     *
     * @param provider l'identifiant du fournisseur (ex: "sendgrid", "twilio")
     * @param payload  le corps de la requête réceptée
     */
    public void publishEvent(String provider, Map<String, Object> payload) {
        InboundWebhookMessage message = new InboundWebhookMessage(provider, payload);
        log.info("[INBOUND PRODUCER] Publication de l'événement webhook [{}] dans RabbitMQ...", provider);
        rabbitTemplate.convertAndSend(inboundExchange, inboundRoutingKey, message);
    }
}

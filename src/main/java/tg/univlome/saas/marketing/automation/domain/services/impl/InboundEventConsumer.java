package tg.univlome.saas.marketing.automation.domain.services.impl;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.automation.application.dtos.requests.InboundWebhookMessage;
import tg.univlome.saas.marketing.automation.application.handlers.WebhookPayloadHandler;

/**
 * Consumer RabbitMQ des événements webhooks entrants.
 *
 * <p>Consomme la file des webhooks et délègue le traitement métier au handler approprié
 * selon le pattern Strategy ({@link WebhookPayloadHandler}).</p>
 *
 * <p>Gestion d'acquittement manuel :</p>
 * <ul>
 *   <li>{@code basicAck(tag, false)} : si le message est traité avec succès ou si le provider n'est pas supporté.</li>
 *   <li>{@code basicNack(tag, false, false)} : en cas d'erreur métier pour ne pas réenfiler le message.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundEventConsumer {

    private final List<WebhookPayloadHandler> webhookHandlers;

    /**
     * Reçoit et traite un message de webhook entrant depuis RabbitMQ.
     *
     * @param message le DTO du message récepté
     * @param channel le canal AMQP pour l'acquittement manuel
     * @param tag     le tag d'envoi du message AMQP
     * @throws IOException en cas d'erreur de communication sur le canal RabbitMQ
     */
    @RabbitListener(queues = "${saas.rabbitmq.queue.inbound}", ackMode = "MANUAL")
    public void processInboundEvent(
            InboundWebhookMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.info("[INBOUND CONSUMER] Message reçu de la file d'ingestion pour le provider [{}]",
                message != null ? message.provider() : "null");

        if (message == null || message.provider() == null) {
            log.warn("[INBOUND CONSUMER] Message nul ou provider non spécifié. Acquittement et suppression.");
            channel.basicAck(tag, false);
            return;
        }

        try {
            // Recherche d'une stratégie supportant le provider
            Optional<WebhookPayloadHandler> handlerOpt = webhookHandlers.stream()
                    .filter(h -> h.supports(message.provider()))
                    .findFirst();

            if (handlerOpt.isPresent()) {
                log.info("[INBOUND CONSUMER] Délégation au handler pour [{}]", message.provider());
                handlerOpt.get().process(message.payload());
                channel.basicAck(tag, false);
                log.info("[INBOUND CONSUMER] Message [{}] traité et acquitté avec succès.", message.provider());
            } else {
                log.warn("[INBOUND CONSUMER] Aucun handler disponible pour le provider [{}]. Message ignoré et acquitté.",
                        message.provider());
                channel.basicAck(tag, false);
            }

        } catch (Exception e) {
            log.error("[INBOUND CONSUMER] Erreur lors du traitement du webhook [{}] : {}",
                    message.provider(), e.getMessage(), e);
            // Non-acquittement sans requeue pour éviter les boucles infinies
            channel.basicNack(tag, false, false);
        }
    }
}

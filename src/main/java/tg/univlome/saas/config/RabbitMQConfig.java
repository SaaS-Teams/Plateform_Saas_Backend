package tg.univlome.saas.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration centralisée de RabbitMQ pour le module d'automatisation.
 *
 * <p>Architecture des files :</p>
 * <pre>
 *   [Producer] ──► automation-exchange ──► workflow-step-queue ──► [Consumer]
 *                                                │ (en cas de rejet définitif)
 *                                                ▼
 *                  automation-dlx ──► workflow-step-dlq ──► [Analyse manuelle / Retry]
 * </pre>
 */
@Configuration
public class RabbitMQConfig {

    // ══════════════════════════════════════════════════════════════
    // File principale (workflow steps)
    // ══════════════════════════════════════════════════════════════

    @Value("${saas.rabbitmq.exchange:automation-exchange}")
    private String automationExchange;

    @Value("${saas.rabbitmq.queue.workflow-step:workflow-step-queue}")
    private String workflowStepQueue;

    @Value("${saas.rabbitmq.routing-key.workflow-step:workflow.step.execute}")
    private String workflowStepRoutingKey;

    // ══════════════════════════════════════════════════════════════
    // Dead Letter (messages en échec définitif)
    // ══════════════════════════════════════════════════════════════

    @Value("${saas.rabbitmq.dlx:automation-dlx}")
    private String deadLetterExchange;

    @Value("${saas.rabbitmq.queue.dlq:workflow-step-dlq}")
    private String deadLetterQueue;

    @Value("${saas.rabbitmq.routing-key.dlq:workflow.step.dead}")
    private String deadLetterRoutingKey;

    // ══════════════════════════════════════════════════════════════
    // Ingestion Webhooks Entrants (Events Inbound)
    // ══════════════════════════════════════════════════════════════

    @Value("${saas.rabbitmq.exchange.inbound:inbound-events-exchange}")
    private String inboundExchange;

    @Value("${saas.rabbitmq.queue.inbound:inbound-events-queue}")
    private String inboundQueue;

    @Value("${saas.rabbitmq.routing-key.inbound:inbound.event.receive}")
    private String inboundRoutingKey;

    // ── File principale ──────────────────────────────────────────

    /**
     * File principale des étapes de workflow.
     *
     * <p>Configurée avec un Dead Letter Exchange (DLX) : lorsqu'un message
     * est rejeté sans requeue ({@code basicNack(tag, false, false)}),
     * RabbitMQ le redirige automatiquement vers la DLQ au lieu de le supprimer.</p>
     */
    @Bean
    public Queue workflowStepQueue() {
        return QueueBuilder.durable(workflowStepQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    /**
     * Exchange principal de type Topic pour le routage des étapes de workflow.
     */
    @Bean
    public TopicExchange automationExchange() {
        return new TopicExchange(automationExchange);
    }

    /**
     * Binding entre la file principale et l'exchange principal.
     */
    @Bean
    public Binding bindingWorkflowStep(Queue workflowStepQueue, TopicExchange automationExchange) {
        return BindingBuilder.bind(workflowStepQueue)
                .to(automationExchange)
                .with(workflowStepRoutingKey);
    }

    // ── Dead Letter Queue (DLQ) ──────────────────────────────────

    /**
     * Exchange dédié aux messages morts (Dead Letter Exchange).
     *
     * <p>Séparé de l'exchange principal pour éviter toute boucle de routage
     * et permettre un monitoring indépendant.</p>
     */
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(deadLetterExchange);
    }

    /**
     * File de stockage des messages morts.
     *
     * <p>Les messages atterrissent ici lorsqu'ils sont rejetés définitivement
     * par le consumer ({@code requeue = false}). Ils restent disponibles
     * pour analyse, debugging ou retry manuel.</p>
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueue).build();
    }

    /**
     * Binding entre la DLQ et le Dead Letter Exchange.
     */
    @Bean
    public Binding bindingDeadLetter(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(deadLetterRoutingKey);
    }

    // ── File & Exchange Webhooks Entrants (Inbound Events) ───────

    /**
     * Exchange dédié à la réception des événements webhooks entrants (SendGrid, Twilio, etc.).
     */
    @Bean
    public TopicExchange inboundExchange() {
        return new TopicExchange(inboundExchange);
    }

    /**
     * File de stockage tampon des événements webhooks entrants.
     */
    @Bean
    public Queue inboundQueue() {
        return QueueBuilder.durable(inboundQueue).build();
    }

    /**
     * Binding entre la file d'événements entrants et l'exchange d'ingestion.
     */
    @Bean
    public Binding bindingInbound(Queue inboundQueue, TopicExchange inboundExchange) {
        return BindingBuilder.bind(inboundQueue)
                .to(inboundExchange)
                .with(inboundRoutingKey);
    }

    // ── Convertisseur JSON ───────────────────────────────────────

    /**
     * Convertisseur Jackson pour la sérialisation/désérialisation
     * automatique des messages RabbitMQ en JSON.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

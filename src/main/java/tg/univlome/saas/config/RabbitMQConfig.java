package tg.univlome.saas.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Noms définis par défaut, surchargeables dans application.properties
    @Value("${saas.rabbitmq.exchange:automation-exchange}")
    private String automationExchange;

    @Value("${saas.rabbitmq.queue.workflow-step:workflow-step-queue}")
    private String workflowStepQueue;

    @Value("${saas.rabbitmq.routing-key.workflow-step:workflow.step.execute}")
    private String workflowStepRoutingKey;

    @Bean
    public Queue workflowStepQueue() {
        return new Queue(workflowStepQueue, true); // true = la file est durable
    }

    @Bean
    public TopicExchange automationExchange() {
        return new TopicExchange(automationExchange);
    }

    @Bean
    public Binding bindingWorkflowStep(Queue workflowStepQueue, TopicExchange automationExchange) {
        return BindingBuilder.bind(workflowStepQueue)
                .to(automationExchange)
                .with(workflowStepRoutingKey);
    }

    // Convertisseur indispensable pour que Spring transforme tes objets Java (ou DTOs) en JSON pour RabbitMQ
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

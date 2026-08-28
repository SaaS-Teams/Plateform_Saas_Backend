package tg.univlome.saas.marketing.automation.domain.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowProducerService;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowProducerServiceImpl implements WorkflowProducerService {

    // RabbitTemplate est fourni par Spring Boot pour dialoguer avec RabbitMQ
    private final RabbitTemplate rabbitTemplate;

    @Value("${saas.rabbitmq.exchange:automation-exchange}")
    private String exchange;

    @Value("${saas.rabbitmq.routing-key.workflow-step:workflow.step.execute}")
    private String routingKey;

    @Override
    public void sendStepToQueue(WorkflowStepMessage message) {
        log.info("Envoi de l'étape {} pour l'exécution {} vers RabbitMQ",
                message.nodeId(), message.executionTrackingId());

        // Envoi du message (RabbitTemplate + Jackson s'occupent de la conversion en JSON)
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}

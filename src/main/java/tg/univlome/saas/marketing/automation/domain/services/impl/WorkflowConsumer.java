package tg.univlome.saas.marketing.automation.domain.services.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowEngineService;

@Slf4j
@Component // Component ou Service, les deux fonctionnent pour un Listener
@RequiredArgsConstructor
public class WorkflowConsumer {
    private final WorkflowEngineService engineService;

    @RabbitListener(queues = "${saas.rabbitmq.queue.workflow-step:workflow-step-queue}")
    public void receiveStepMessage(WorkflowStepMessage message) {
        log.info("Message intercepté par RabbitMQ -> Exécution : {}, Étape : {}",
                message.executionTrackingId(), message.nodeId());

        try {
            // On délègue tout le travail complexe au moteur
            engineService.processStep(message);
        } catch (Exception e) {
            log.error("Erreur critique interceptée dans le Consumer RabbitMQ pour l'exécution {}",
                    message.executionTrackingId(), e);
            throw e;
        }
    }
}

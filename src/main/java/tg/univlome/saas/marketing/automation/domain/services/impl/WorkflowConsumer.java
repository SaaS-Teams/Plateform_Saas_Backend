package tg.univlome.saas.marketing.automation.domain.services.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;

@Slf4j
@Component // Component ou Service, les deux fonctionnent pour un Listener
@RequiredArgsConstructor
public class WorkflowConsumer {

    @RabbitListener(queues = "${saas.rabbitmq.queue.workflow-step:workflow-step-queue}")
    public void receiveStepMessage(WorkflowStepMessage message) {
        log.info("Message reçu depuis RabbitMQ ! Exécution : {}, Étape : {}, Action : {}",
                message.executionTrackingId(), message.nodeId(), message.actionType());

        try {
            // TODO: Ajouter ici la logique métier de l'aiguilleur interne
            // (Si action == SEND_EMAIL -> appeler EmailService, etc.)

            log.info("Étape {} traitée avec succès.", message.nodeId());
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'étape {} pour l'exécution {}",
                    message.nodeId(), message.executionTrackingId(), e);
            // Dans un vrai système, on gèrerait ici une mise en "Dead Letter Queue"
            throw e;
        }
    }
}

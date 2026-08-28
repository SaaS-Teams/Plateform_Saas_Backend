package tg.univlome.saas.marketing.automation.domain.services;

import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;

public interface WorkflowEngineService {
    /**
     * Reçoit le message de RabbitMQ et exécute l'action demandée (Email, Attente, etc.)
     */
    void processStep(WorkflowStepMessage message);
}

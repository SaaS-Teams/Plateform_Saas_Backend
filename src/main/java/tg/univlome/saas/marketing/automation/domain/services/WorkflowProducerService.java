package tg.univlome.saas.marketing.automation.domain.services;

import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowStepMessage;

public interface WorkflowProducerService {
    void sendStepToQueue(WorkflowStepMessage message);

}

package tg.univlome.saas.marketing.automation.domain.services;


import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowResponse;

import java.util.List;
import java.util.UUID;

public interface WorkflowService {

    WorkflowResponse createWorkflow(WorkflowRequest request);

    WorkflowResponse getWorkflowByTrackingId(UUID trackingId);

    List<WorkflowResponse> getAllWorkflows();

    WorkflowResponse updateWorkflow(UUID trackingId, WorkflowRequest request);

    void deleteWorkflow(UUID trackingId);
}

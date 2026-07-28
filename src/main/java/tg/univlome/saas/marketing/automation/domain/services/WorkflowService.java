package tg.univlome.saas.marketing.automation.domain.services;



import java.util.List;
import java.util.UUID;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowResponse;

public interface WorkflowService {
    WorkflowResponse createWorkflow(WorkflowRequest request);
    WorkflowResponse getWorkflowByTrackingId(UUID trackingId);
    List<WorkflowResponse> getAllWorkflows();
    WorkflowResponse updateWorkflow(UUID trackingId, WorkflowRequest request);
    void deleteWorkflow(UUID trackingId);
}

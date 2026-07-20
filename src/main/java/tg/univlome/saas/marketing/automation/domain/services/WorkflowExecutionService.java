package tg.univlome.saas.marketing.automation.domain.services;


import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowExecutionRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowExecutionResponse;
import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;

import java.util.List;
import java.util.UUID;

public interface WorkflowExecutionService {

    WorkflowExecutionResponse startExecution(WorkflowExecutionRequest request);

    WorkflowExecutionResponse getExecutionByTrackingId(UUID executionTrackingId);

    List<WorkflowExecutionResponse> getExecutionsByWorkflow(UUID workflowTrackingId);

    List<WorkflowExecutionResponse> getExecutionsByContact(Long contactId);

    WorkflowExecutionResponse updateExecutionStatus(UUID executionTrackingId, ExecutionStatus newStatus, String currentNodeId, String errorDetails);
}

package tg.univlome.saas.marketing.automation.application.mappers;


import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowResponse;
import tg.univlome.saas.marketing.automation.domain.models.Workflow;

@Component
public class WorkflowMapper {
    public WorkflowResponse toResponse(Workflow workflow) {
        if (workflow == null) {
            throw new IllegalArgumentException("Le scénario (workflow) ne peut pas être null");
        }
        return new WorkflowResponse(
                workflow.getTrackingId(),
                workflow.getName(),
                workflow.getDescription(),
                workflow.getStatus(),
                workflow.getTriggerType(),
                workflow.getFlowData(),
                workflow.getCreatedAt(),
                workflow.getUpdatedAt()
        );
    }
    public List<WorkflowResponse> toResponseList(List<Workflow> workflows) {
        if (workflows == null) {
            return new ArrayList<>();
        }
        return workflows.stream()
                .map(this::toResponse)
                .toList();
    }
    public Workflow toEntity(WorkflowRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La requête de création ne peut pas être null");
        }
        Workflow workflow = new Workflow();
        workflow.setName(request.name());
        workflow.setDescription(request.description());
        workflow.setStatus(request.status());
        workflow.setTriggerType(request.triggerType());
        workflow.setFlowData(request.flowData());
        return workflow;
    }
}

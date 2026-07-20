package tg.univlome.saas.marketing.automation.application.mappers;


import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowExecutionRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowExecutionResponse;
import tg.univlome.saas.marketing.automation.domain.models.Workflow;
import tg.univlome.saas.marketing.automation.domain.models.WorkflowExecutionLog;
import tg.univlome.saas.marketing.contact.domain.models.Contact;

@Component
public class WorkflowExecutionMapper {
    public WorkflowExecutionResponse toResponse(WorkflowExecutionLog log) {
        if (log == null) {
            throw new IllegalArgumentException("Le log d'exécution ne peut pas être null");
        }
        return new WorkflowExecutionResponse(
                log.getExecutionTrackingId(),
                log.getWorkflow().getTrackingId(),
                log.getContact().getId(),
                log.getStatus(),
                log.getCurrentNodeId(),
                log.getErrorDetails(),
                log.getStartedAt(),
                log.getCompletedAt()
        );
    }
    public List<WorkflowExecutionResponse> toResponseList(List<WorkflowExecutionLog> logs) {
        if (logs == null) {
            return new ArrayList<>();
        }
        return logs.stream()
                .map(this::toResponse)
                .toList();
    }
    public WorkflowExecutionLog toEntity(WorkflowExecutionRequest request, Workflow workflow, Contact contact) {
        if (request == null || workflow == null || contact == null) {
            throw new IllegalArgumentException("Les données fournies pour l'exécution sont incomplètes");
        }
        WorkflowExecutionLog log = new WorkflowExecutionLog();
        log.setWorkflow(workflow);
        log.setContact(contact);
        log.setStatus(request.status());
        log.setCurrentNodeId(request.currentNodeId());
        return log;
    }
}

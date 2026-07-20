package tg.univlome.saas.marketing.automation.domain.services.impl;



import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowExecutionRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowExecutionResponse;
import tg.univlome.saas.marketing.automation.application.mappers.WorkflowExecutionMapper;
import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;
import tg.univlome.saas.marketing.automation.domain.models.Workflow;
import tg.univlome.saas.marketing.automation.domain.models.WorkflowExecutionLog;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowExecutionService;
import tg.univlome.saas.marketing.automation.repositories.WorkflowExecutionLogRepository;
import tg.univlome.saas.marketing.automation.repositories.WorkflowRepository;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository; // Assure-toi que ce repo existe dans ton module Contact
import tg.univlome.saas.shared.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {
    private final WorkflowExecutionLogRepository executionRepository;
    private final WorkflowRepository workflowRepository;
    private final ContactRepository contactRepository;
    private final WorkflowExecutionMapper executionMapper;
    @Override
    @Transactional
    public WorkflowExecutionResponse startExecution(WorkflowExecutionRequest request) {
        // 1. Récupération du Workflow
        Workflow workflow = workflowRepository.findByTrackingId(request.workflowTrackingId())
                .orElseThrow(() -> new ResourceNotFoundException("Scénario introuvable : " + request.workflowTrackingId()));
        // 2. Récupération du Contact
        Contact contact = contactRepository.findById(request.contactId())
                .orElseThrow(() -> new ResourceNotFoundException("Contact introuvable : " + request.contactId()));
        // 3. Utilisation de ton mapper manuel avec les entités récupérées
        WorkflowExecutionLog log = executionMapper.toEntity(request, workflow, contact);
        WorkflowExecutionLog savedLog = executionRepository.save(log);
        return executionMapper.toResponse(savedLog);
    }
    @Override
    @Transactional(readOnly = true)
    public WorkflowExecutionResponse getExecutionByTrackingId(UUID executionTrackingId) {
        WorkflowExecutionLog log = findExecutionOrThrow(executionTrackingId);
        return executionMapper.toResponse(log);
    }
    @Override
    @Transactional(readOnly = true)
    public List<WorkflowExecutionResponse> getExecutionsByWorkflow(UUID workflowTrackingId) {
        // On récupère d'abord le Workflow pour avoir son ID interne
        Workflow workflow = workflowRepository.findByTrackingId(workflowTrackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Scénario introuvable"));
        List<WorkflowExecutionLog> logs = executionRepository.findByWorkflowId(workflow.getId());
        return executionMapper.toResponseList(logs);
    }
    @Override
    @Transactional(readOnly = true)
    public List<WorkflowExecutionResponse> getExecutionsByContact(Long contactId) {
        List<WorkflowExecutionLog> logs = executionRepository.findByContactId(contactId);
        return executionMapper.toResponseList(logs);
    }
    @Override
    @Transactional
    public WorkflowExecutionResponse updateExecutionStatus(UUID executionTrackingId, 
            ExecutionStatus newStatus, String currentNodeId, String errorDetails) {
        WorkflowExecutionLog log = findExecutionOrThrow(executionTrackingId);
        log.setStatus(newStatus);
        log.setCurrentNodeId(currentNodeId);
        if (errorDetails != null) {
            log.setErrorDetails(errorDetails);
        }
        if (newStatus == ExecutionStatus.COMPLETED || newStatus == ExecutionStatus.FAILED || newStatus == ExecutionStatus.CANCELLED) {
            log.setCompletedAt(LocalDateTime.now());
        }
        WorkflowExecutionLog updatedLog = executionRepository.save(log);
        return executionMapper.toResponse(updatedLog);
    }
    private WorkflowExecutionLog findExecutionOrThrow(UUID executionTrackingId) {
        return executionRepository.findByExecutionTrackingId(executionTrackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Exécution introuvable : " + executionTrackingId));
    }
}

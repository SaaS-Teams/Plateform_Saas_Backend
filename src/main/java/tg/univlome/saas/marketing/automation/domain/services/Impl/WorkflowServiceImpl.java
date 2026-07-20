package tg.univlome.saas.marketing.automation.domain.services.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowResponse;
import tg.univlome.saas.marketing.automation.application.mappers.WorkflowMapper;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowService;
import tg.univlome.saas.marketing.automation.domain.models.Workflow;
import tg.univlome.saas.marketing.automation.repositories.WorkflowRepository;
import tg.univlome.saas.shared.exceptions.ResourceNotFoundException; // Utilise l'exception de ton dossier shared

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;

    @Override
    @Transactional
    public WorkflowResponse createWorkflow(WorkflowRequest request) {
        Workflow workflow = workflowMapper.toEntity(request);
        Workflow savedWorkflow = workflowRepository.save(workflow);
        return workflowMapper.toResponse(savedWorkflow);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflowByTrackingId(UUID trackingId) {
        Workflow workflow = findWorkflowOrThrow(trackingId);
        return workflowMapper.toResponse(workflow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowResponse> getAllWorkflows() {
        List<Workflow> workflows = workflowRepository.findAll();
        return workflowMapper.toResponseList(workflows);
    }

    @Override
    @Transactional
    public WorkflowResponse updateWorkflow(UUID trackingId, WorkflowRequest request) {
        Workflow existingWorkflow = findWorkflowOrThrow(trackingId);

        existingWorkflow.setName(request.name());
        existingWorkflow.setDescription(request.description());
        existingWorkflow.setStatus(request.status());
        existingWorkflow.setTriggerType(request.triggerType());
        existingWorkflow.setFlowData(request.flowData());

        Workflow updatedWorkflow = workflowRepository.save(existingWorkflow);
        return workflowMapper.toResponse(updatedWorkflow);
    }

    @Override
    @Transactional
    public void deleteWorkflow(UUID trackingId) {
        Workflow existingWorkflow = findWorkflowOrThrow(trackingId);
        workflowRepository.delete(existingWorkflow);
    }

    // Méthode utilitaire interne
    private Workflow findWorkflowOrThrow(UUID trackingId) {
        return workflowRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Scénario introuvable avec l'ID : " + trackingId));
    }
}
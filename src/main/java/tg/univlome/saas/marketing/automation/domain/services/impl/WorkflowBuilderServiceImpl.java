package tg.univlome.saas.marketing.automation.domain.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.automation.domain.models.Workflow;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowBuilderService;
import tg.univlome.saas.marketing.automation.repositories.WorkflowRepository;
import tg.univlome.saas.shared.exceptions.ResourceNotFoundException;
import tg.univlome.saas.web.dtos.workflow.SaveWorkflowRequest;
import tg.univlome.saas.web.dtos.workflow.WorkflowCanvasResponse;
import tg.univlome.saas.web.dtos.workflow.WorkflowEdgeDto;
import tg.univlome.saas.web.dtos.workflow.WorkflowNodeDto;

/**
 * Implémentation du service de gestion du constructeur visuel de workflow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowBuilderServiceImpl implements WorkflowBuilderService {

    private final WorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public WorkflowCanvasResponse saveCanvas(Long workflowId, SaveWorkflowRequest request) {
        log.info("[WORKFLOW BUILDER] Sauvegarde du canvas visuel pour le workflow ID [{}]", workflowId);

        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow non trouvé avec l'ID : " + workflowId));

        // Règle métier : vérifier la présence d'au moins un déclencheur ("trigger" ou "start")
        List<WorkflowNodeDto> nodes = request.nodes() != null ? request.nodes() : Collections.emptyList();
        boolean hasTrigger = nodes.stream()
                .anyMatch(node -> node.type() != null
                        && (node.type().toLowerCase().contains("trigger") || node.type().toLowerCase().contains("start")));

        if (!hasTrigger) {
            throw new IllegalArgumentException("Le workflow doit contenir au moins un déclencheur (type contenant 'trigger' ou 'start').");
        }

        // Construction et sérialisation du canvas visuel
        Map<String, Object> canvasMap = new HashMap<>();
        canvasMap.put("nodes", nodes);
        canvasMap.put("edges", request.edges() != null ? request.edges() : Collections.emptyList());

        try {
            String canvasJson = objectMapper.writeValueAsString(canvasMap);
            workflow.setCanvasDefinition(canvasJson);

            // Mise à jour du nom et de la description
            if (request.name() != null && !request.name().isBlank()) {
                workflow.setName(request.name());
            }
            if (request.description() != null) {
                workflow.setDescription(request.description());
            }

            // Si flowData n'est pas encore initialisé, on initialise un JSON minimum pour satisfaire le not-null DB
            if (workflow.getFlowData() == null || workflow.getFlowData().isBlank()) {
                workflow.setFlowData(canvasJson);
            }

            Workflow savedWorkflow = workflowRepository.save(workflow);
            log.info("[WORKFLOW BUILDER] Canvas du workflow [{}] sauvegardé avec succès.", savedWorkflow.getId());

            return new WorkflowCanvasResponse(
                    savedWorkflow.getId(),
                    savedWorkflow.getName(),
                    savedWorkflow.getDescription(),
                    nodes,
                    request.edges() != null ? request.edges() : Collections.emptyList()
            );

        } catch (JsonProcessingException e) {
            log.error("[WORKFLOW BUILDER] Erreur lors de la sérialisation du canvas en JSON pour le workflow [{}]: {}",
                    workflowId, e.getMessage(), e);
            throw new IllegalStateException("Erreur de sérialisation JSON du canvas du workflow", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowCanvasResponse getCanvas(Long workflowId) {
        log.info("[WORKFLOW BUILDER] Récupération du canvas visuel pour le workflow ID [{}]", workflowId);

        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow non trouvé avec l'ID : " + workflowId));

        String canvasJson = workflow.getCanvasDefinition();
        List<WorkflowNodeDto> nodes = Collections.emptyList();
        List<WorkflowEdgeDto> edges = Collections.emptyList();

        if (canvasJson != null && !canvasJson.isBlank()) {
            try {
                Map<String, Object> canvasMap = objectMapper.readValue(canvasJson, new TypeReference<>() {});

                if (canvasMap.containsKey("nodes")) {
                    nodes = objectMapper.convertValue(canvasMap.get("nodes"), new TypeReference<List<WorkflowNodeDto>>() {});
                }
                if (canvasMap.containsKey("edges")) {
                    edges = objectMapper.convertValue(canvasMap.get("edges"), new TypeReference<List<WorkflowEdgeDto>>() {});
                }
            } catch (JsonProcessingException e) {
                log.error("[WORKFLOW BUILDER] Erreur de désérialisation du canvas JSON pour le workflow [{}]: {}",
                        workflowId, e.getMessage(), e);
                throw new IllegalStateException("Erreur de désérialisation JSON du canvas", e);
            }
        }

        return new WorkflowCanvasResponse(
                workflow.getId(),
                workflow.getName(),
                workflow.getDescription(),
                nodes,
                edges
        );
    }
}

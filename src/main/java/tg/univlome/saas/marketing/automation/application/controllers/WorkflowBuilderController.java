package tg.univlome.saas.marketing.automation.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowBuilderService;
import tg.univlome.saas.web.dtos.workflow.SaveWorkflowRequest;
import tg.univlome.saas.web.dtos.workflow.WorkflowCanvasResponse;

/**
 * Contrôleur REST pour le constructeur visuel de workflows (Frontend React Builder).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/builder/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflow Builder", description = "API REST pour le constructeur de workflows No-Code")
public class WorkflowBuilderController {

    private final WorkflowBuilderService workflowBuilderService;

    @PutMapping("/{id}/canvas")
    @Operation(summary = "Sauvegarde de la configuration visuelle (nœuds et liens) du canvas de workflow")
    public ResponseEntity<WorkflowCanvasResponse> saveCanvas(
            @PathVariable("id") Long workflowId,
            @Valid @RequestBody SaveWorkflowRequest request) {

        log.info("[API BUILDER] Sauvegarde canvas demandée pour le workflow ID [{}]", workflowId);
        WorkflowCanvasResponse response = workflowBuilderService.saveCanvas(workflowId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/canvas")
    @Operation(summary = "Récupération de la configuration visuelle du canvas pour réaffichage frontend")
    public ResponseEntity<WorkflowCanvasResponse> getCanvas(
            @PathVariable("id") Long workflowId) {

        log.info("[API BUILDER] Récupération du canvas demandée pour le workflow ID [{}]", workflowId);
        WorkflowCanvasResponse response = workflowBuilderService.getCanvas(workflowId);
        return ResponseEntity.ok(response);
    }
}

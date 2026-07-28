package tg.univlome.saas.marketing.automation.application.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowExecutionRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowExecutionResponse;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowExecutionService;

@RestController
@RequestMapping("/api/workflow-executions")
@RequiredArgsConstructor
@Tag(name = "Automation - Exécutions", description = "Endpoints pour le suivi (journal de bord) des contacts à l'intérieur des scénarios.")
public class WorkflowExecutionController {
    private final WorkflowExecutionService executionService;
    @PostMapping
    @Operation(summary = "Démarrer une nouvelle exécution (Test/Manuel)",
            description = "Permet de forcer l'entrée d'un contact dans un scénario. Normalement, cette action "
                    + "est déclenchée automatiquement par des événements système, "
                    + "mais cet endpoint est utile pour les tests ou les déclenchements manuels.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exécution démarrée et tracée"),
            @ApiResponse(responseCode = "404", description = "Scénario ou Contact introuvable")
    })
    public ResponseEntity<WorkflowExecutionResponse> startExecution(@Valid @RequestBody WorkflowExecutionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(executionService.startExecution(request));
    }
    @GetMapping("/{executionTrackingId}")
    @Operation(summary = "Vérifier le statut d'une exécution",
            description = "Récupère les détails d'un parcours spécifique (où est bloqué le contact, "
                    + "a-t-il reçu l'email, y a-t-il une erreur ?).")
    public ResponseEntity<WorkflowExecutionResponse> getExecutionByTrackingId(
            @Parameter(description = "L'UUID spécifique à cette exécution") @PathVariable UUID executionTrackingId) {
        return ResponseEntity.ok(executionService.getExecutionByTrackingId(executionTrackingId));
    }
    @GetMapping("/workflow/{workflowTrackingId}")
    @Operation(summary = "Lister les participants d'un scénario",
            description = "Récupère la liste de tous les contacts qui sont en train de parcourir "
                    + "(ou ont terminé) un scénario spécifique. Utile pour les statistiques de la campagne.")
    public ResponseEntity<List<WorkflowExecutionResponse>> getExecutionsByWorkflow(
            @Parameter(description = "L'UUID public du scénario") @PathVariable UUID workflowTrackingId) {
        return ResponseEntity.ok(executionService.getExecutionsByWorkflow(workflowTrackingId));
    }
    @GetMapping("/contact/{contactId}")
    @Operation(summary = "Historique d'automation d'un contact",
            description = "Récupère tous les scénarios par lesquels un contact spécifique est passé (historique CRM).")
    public ResponseEntity<List<WorkflowExecutionResponse>> getExecutionsByContact(
            @Parameter(description = "L'ID interne du contact") @PathVariable Long contactId) {
        return ResponseEntity.ok(executionService.getExecutionsByContact(contactId));
    }
}

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.automation.application.dtos.requests.WorkflowRequest;
import tg.univlome.saas.marketing.automation.application.dtos.responses.WorkflowResponse;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowService;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
@Tag(name = "Automation - Workflows", description = "Endpoints pour la gestion administrative des scénarios (l'éditeur visuel)")
public class WorkflowController {
    private final WorkflowService workflowService;
    @PostMapping
    @Operation(summary = "Créer un nouveau scénario",
            description = "Enregistre un nouveau parcours d'automatisation. Le champ 'flowData' doit "
                    + "contenir le JSON brut généré par l'éditeur visuel (React Flow par exemple).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Scénario créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides (JSON malformé ou champs manquants)")
    })
    public ResponseEntity<WorkflowResponse> createWorkflow(@Valid @RequestBody WorkflowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflowService.createWorkflow(request));
    }
    @GetMapping
    @Operation(summary = "Lister tous les scénarios",
            description = "Récupère la liste de toutes les campagnes d'automatisation (Brouillons, "
                    + "Actives et En Pause) pour l'affichage dans le tableau de bord.")
    public ResponseEntity<List<WorkflowResponse>> getAllWorkflows() {
        return ResponseEntity.ok(workflowService.getAllWorkflows());
    }
    @GetMapping("/{trackingId}")
    @Operation(summary = "Obtenir les détails d'un scénario",
            description = "Récupère la structure complète d'un parcours (incluant son flowData JSON) "
                    + "via son UUID public. Idéal pour recharger un parcours dans l'éditeur visuel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scénario trouvé"),
            @ApiResponse(responseCode = "404", description = "Scénario introuvable avec ce trackingId")
    })
    public ResponseEntity<WorkflowResponse> getWorkflowByTrackingId(
            @Parameter(description = "L'UUID public du scénario") @PathVariable UUID trackingId) {
        return ResponseEntity.ok(workflowService.getWorkflowByTrackingId(trackingId));
    }
    @PutMapping("/{trackingId}")
    @Operation(summary = "Mettre à jour un scénario",
            description = "Écrase la définition d'un scénario existant. Utilisé lorsque "
                    + "l'utilisateur clique sur 'Sauvegarder' dans l'éditeur visuel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scénario mis à jour"),
            @ApiResponse(responseCode = "404", description = "Scénario introuvable")
    })
    public ResponseEntity<WorkflowResponse> updateWorkflow(
            @Parameter(description = "L'UUID public du scénario") @PathVariable UUID trackingId,
            @Valid @RequestBody WorkflowRequest request) {
        return ResponseEntity.ok(workflowService.updateWorkflow(trackingId, request));
    }
    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer un scénario", description = "Supprime définitivement un scénario d'automatisation.")
    @ApiResponse(responseCode = "204", description = "Scénario supprimé avec succès")
    public ResponseEntity<Void> deleteWorkflow(
            @Parameter(description = "L'UUID public du scénario") @PathVariable UUID trackingId) {
        workflowService.deleteWorkflow(trackingId);
        return ResponseEntity.noContent().build();
    }
}

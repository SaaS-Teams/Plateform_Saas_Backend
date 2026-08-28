package tg.univlome.saas.web.dtos.workflow;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * DTO de requête de sauvegarde du canvas visuel d'un workflow.
 */
public record SaveWorkflowRequest(
        @NotBlank(message = "Le nom du workflow est obligatoire")
        String name,

        String description,

        List<WorkflowNodeDto> nodes,

        List<WorkflowEdgeDto> edges
) {
}

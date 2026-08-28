package tg.univlome.saas.web.dtos.workflow;

import java.util.List;

/**
 * DTO de réponse contenant le graphe d'un workflow à afficher dans le frontend React.
 */
public record WorkflowCanvasResponse(
        Long id,
        String name,
        String description,
        List<WorkflowNodeDto> nodes,
        List<WorkflowEdgeDto> edges
) {
}

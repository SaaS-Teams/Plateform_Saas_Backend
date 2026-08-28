package tg.univlome.saas.web.dtos.workflow;

/**
 * DTO représentant une connexion (edge/lien) entre deux nœuds graphiques.
 */
public record WorkflowEdgeDto(
        String id,
        String source,
        String target
) {
}

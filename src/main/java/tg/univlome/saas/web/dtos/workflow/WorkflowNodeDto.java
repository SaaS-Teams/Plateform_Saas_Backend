package tg.univlome.saas.web.dtos.workflow;

import java.util.Map;

/**
 * DTO représentant un nœud graphique dans le constructeur de workflow No-Code (React Flow / React).
 */
public record WorkflowNodeDto(
        String id,
        String type,
        Map<String, Object> data,
        Map<String, Object> position
) {
}

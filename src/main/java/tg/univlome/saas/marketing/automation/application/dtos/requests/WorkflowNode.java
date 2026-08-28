package tg.univlome.saas.marketing.automation.application.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Représentation d'un nœud dans l'arbre logique d'un workflow.
 *
 * <p>Ce record est désérialisé depuis le champ JSONB {@code flowData} de l'entité
 * {@code Workflow}. Il supporte deux modes de chaînage :</p>
 *
 * <ul>
 *   <li><b>Séquentiel</b> : le champ {@code nextStepId} pointe vers le prochain nœud.</li>
 *   <li><b>Conditionnel (IF/ELSE)</b> : si {@code type} vaut {@code "CONDITION"},
 *       le moteur évalue la condition et emprunte {@code nextStepIdIfTrue}
 *       ou {@code nextStepIdIfFalse}.</li>
 * </ul>
 *
 * <p><b>Rétrocompatibilité</b> : les nouveaux champs sont nullable. Un JSON existant
 * sans ces champs sera désérialisé avec des valeurs {@code null}, ce qui préserve
 * le comportement séquentiel d'origine.</p>
 *
 * <p>Exemple de nœud conditionnel dans le JSON {@code flowData} :</p>
 * <pre>{@code
 * {
 *   "id": "node_condition_1",
 *   "type": "CONDITION",
 *   "data": {
 *     "conditionField": "contact.tag",
 *     "conditionOperator": "EQUALS",
 *     "conditionValue": "VIP"
 *   },
 *   "next": null,
 *   "nextIfTrue": "node_email_vip",
 *   "nextIfFalse": "node_email_standard"
 * }
 * }</pre>
 */
public record WorkflowNode(
        // ── Identité du nœud ─────────────────────────────────────
        @JsonProperty("id")
        String nodeId,

        // Type du nœud : ACTION_EMAIL, WAIT, CONDITION, etc.
        String type,

        // Paramètres libres du nœud (templateId, subject, days, etc.)
        Map<String, Object> data,

        // ── Chaînage séquentiel (nœuds simples) ──────────────────
        // Jackson lira "next", mais en Java c'est "nextStepId"
        @JsonProperty("next")
        String nextStepId,

        // ── Chaînage conditionnel (nœuds IF/ELSE) ────────────────

        // Nœud suivant si la condition est VRAIE
        @JsonProperty("nextIfTrue")
        String nextStepIdIfTrue,

        // Nœud suivant si la condition est FAUSSE
        @JsonProperty("nextIfFalse")
        String nextStepIdIfFalse
) {}

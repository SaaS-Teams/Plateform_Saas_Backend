package tg.univlome.saas.marketing.automation.domain.services;

import java.util.Map;

/**
 * Service d'évaluation des conditions pour les nœuds de type {@code CONDITION}.
 *
 * <p>Le moteur d'exécution ({@code WorkflowEngineServiceImpl}) délègue à ce service
 * la décision de routage (branche IF ou ELSE) lors du traitement d'un nœud conditionnel.</p>
 */
public interface ConditionEvaluatorService {

    /**
     * Évalue la condition définie dans les données d'un nœud {@code CONDITION}.
     *
     * <p>Les paramètres attendus dans {@code nodeData} sont :</p>
     * <ul>
     *   <li>{@code conditionField} — le champ du contact à évaluer
     *   (ex: {@code "contact.tag"}, {@code "contact.email"})</li>
     *   <li>{@code conditionOperator} — l'opérateur de comparaison
     *   (ex: {@code "EQUALS"}, {@code "CONTAINS"}, {@code "IS_EMPTY"})</li>
     *   <li>{@code conditionValue} — la valeur de référence attendue
     *   (ex: {@code "VIP"}, {@code "premium"})</li>
     * </ul>
     *
     * @param nodeData  les données du nœud contenant la définition de la condition
     * @param contactId l'identifiant (sous forme de chaîne) du contact en cours de traitement
     * @return {@code true} si la condition est satisfaite (branche IF),
     *         {@code false} sinon (branche ELSE)
     */
    boolean evaluateCondition(Map<String, Object> nodeData, String contactId);
}

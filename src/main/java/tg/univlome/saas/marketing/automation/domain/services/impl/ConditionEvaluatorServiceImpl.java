package tg.univlome.saas.marketing.automation.domain.services.impl;

import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.automation.domain.services.ConditionEvaluatorService;

/**
 * Implémentation du service d'évaluation des conditions.
 *
 * <p>Version initiale avec une logique basique de comparaison de chaînes.
 * La résolution de la valeur réelle du champ du contact est simulée
 * (à remplacer par une requête JPA/MongoDB lorsque le modèle Contact
 * disposera de champs dynamiques ou de tags).</p>
 *
 * <p>Opérateurs supportés :</p>
 * <ul>
 *   <li>{@code EQUALS} — égalité stricte</li>
 *   <li>{@code NOT_EQUALS} — différence</li>
 *   <li>{@code CONTAINS} — la valeur réelle contient la valeur attendue</li>
 *   <li>{@code IS_EMPTY} — le champ est null ou vide</li>
 *   <li>{@code IS_NOT_EMPTY} — le champ est renseigné</li>
 * </ul>
 */
@Slf4j
@Service
public class ConditionEvaluatorServiceImpl implements ConditionEvaluatorService {

    @Override
    public boolean evaluateCondition(Map<String, Object> nodeData, String contactId) {
        // 1. Extraction des paramètres de condition depuis la Map du nœud
        String field = (String) nodeData.getOrDefault("conditionField", "");
        String operator = (String) nodeData.getOrDefault("conditionOperator", "EQUALS");
        String expectedValue = (String) nodeData.getOrDefault("conditionValue", "");

        // 2. Résolution de la valeur réelle du champ pour ce contact
        //    TODO : Remplacer par une requête réelle vers Contact / ContactTag
        String actualValue = resolveFieldValue(field, contactId);

        // 3. Évaluation de la condition
        boolean result = evaluate(operator, actualValue, expectedValue);

        log.info("Condition évaluée pour le contact [{}] : '{}' {} '{}' → {}",
                contactId, field, operator, expectedValue, result ? "VRAI" : "FAUX");

        return result;
    }

    // ── Méthodes privées ─────────────────────────────────────────

    /**
     * Résout la valeur réelle d'un champ pour un contact donné.
     *
     * <p>Implémentation simulée : retourne une chaîne vide.
     * Dans une version future, ce service interrogera la base de données
     * (ex: {@code contactRepository.findFieldValue(contactId, field)}).</p>
     *
     * @param field     le nom du champ à résoudre (ex: "contact.tag")
     * @param contactId l'identifiant du contact
     * @return la valeur actuelle du champ, ou une chaîne vide si inconnue
     */
    private String resolveFieldValue(String field, String contactId) {
        log.debug("Résolution du champ '{}' pour le contact [{}] (simulation — retourne vide)", field, contactId);
        // Simulation : la valeur réelle sera récupérée en BDD plus tard
        return "";
    }

    /**
     * Applique l'opérateur de comparaison entre la valeur réelle et la valeur attendue.
     *
     * @param operator      l'opérateur (EQUALS, NOT_EQUALS, CONTAINS, IS_EMPTY, IS_NOT_EMPTY)
     * @param actualValue   la valeur réelle du champ
     * @param expectedValue la valeur de référence définie dans le scénario
     * @return le résultat de l'évaluation
     */
    private boolean evaluate(String operator, String actualValue, String expectedValue) {
        return switch (operator) {
            case "EQUALS" -> Objects.equals(actualValue, expectedValue);
            case "NOT_EQUALS" -> !Objects.equals(actualValue, expectedValue);
            case "CONTAINS" -> actualValue != null && actualValue.contains(expectedValue);
            case "IS_EMPTY" -> actualValue == null || actualValue.isBlank();
            case "IS_NOT_EMPTY" -> actualValue != null && !actualValue.isBlank();
            default -> {
                log.warn("Opérateur de condition inconnu : '{}'. Retourne false par défaut.", operator);
                yield false;
            }
        };
    }
}

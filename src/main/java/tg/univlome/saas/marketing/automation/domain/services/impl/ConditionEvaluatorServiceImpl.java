package tg.univlome.saas.marketing.automation.domain.services.impl;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.automation.domain.services.ConditionEvaluatorService;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;
import tg.univlome.saas.marketing.contact.repositories.ContactSegmentRepository;

/**
 * Service d'évaluation des conditions pour les nœuds de type {@code CONDITION}.
 *
 * <p>Résout la valeur réelle du champ demandé en interrogeant directement
 * la base de données via {@link ContactRepository} et {@link ContactSegmentRepository},
 * puis applique l'opérateur de comparaison défini dans le nœud.</p>
 *
 * <h3>Champs supportés ({@code conditionField})</h3>
 * <ul>
 *   <li>{@code contact.email}         — adresse e-mail du contact</li>
 *   <li>{@code contact.firstName}     — prénom</li>
 *   <li>{@code contact.lastName}      — nom de famille</li>
 *   <li>{@code contact.city}          — ville</li>
 *   <li>{@code contact.country}       — pays</li>
 *   <li>{@code contact.consentStatus} — statut de consentement (OPT_IN, OPT_OUT, PENDING)</li>
 *   <li>{@code contact.segment}       — appartenance à un segment (par UUID ou nom)</li>
 * </ul>
 *
 * <h3>Opérateurs supportés ({@code conditionOperator})</h3>
 * <ul>
 *   <li>{@code EQUALS}       — égalité stricte (insensible à la casse)</li>
 *   <li>{@code NOT_EQUALS}   — différence</li>
 *   <li>{@code CONTAINS}     — la valeur contient la chaîne recherchée</li>
 *   <li>{@code STARTS_WITH}  — la valeur commence par la chaîne</li>
 *   <li>{@code ENDS_WITH}    — la valeur se termine par la chaîne</li>
 *   <li>{@code IS_EMPTY}     — le champ est null ou vide</li>
 *   <li>{@code IS_NOT_EMPTY} — le champ est renseigné</li>
 *   <li>{@code IN_SEGMENT}   — le contact appartient au segment ({@code conditionValue} = UUID du segment)</li>
 *   <li>{@code NOT_IN_SEGMENT} — le contact n'appartient pas au segment</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConditionEvaluatorServiceImpl implements ConditionEvaluatorService {

    private final ContactRepository contactRepository;
    private final ContactSegmentRepository contactSegmentRepository;

    @Override
    public boolean evaluateCondition(Map<String, Object> nodeData, String contactId) {
        String field    = (String) nodeData.getOrDefault("conditionField", "");
        String operator = (String) nodeData.getOrDefault("conditionOperator", "EQUALS");
        String expected = (String) nodeData.getOrDefault("conditionValue", "");

        // Les opérateurs de segment court-circuitent la résolution de champ textuel
        if ("IN_SEGMENT".equals(operator) || "NOT_IN_SEGMENT".equals(operator)) {
            boolean result = evaluateSegmentMembership(contactId, expected, operator);
            log.info("Condition [{}] évaluée pour le contact [{}] → segment '{}' → {}",
                    operator, contactId, expected, result ? "VRAI" : "FAUX");
            return result;
        }

        // Résolution du champ texte depuis la base de données
        Contact contact = contactRepository.findById(Long.valueOf(contactId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contact introuvable pour l'évaluation de condition, id=" + contactId));

        String actualValue = resolveContactField(field, contact);
        boolean result = evaluateOperator(operator, actualValue, expected);

        log.info("Condition [{}] évaluée pour le contact [{}] : champ='{}', attendu='{}', réel='{}' → {}",
                operator, contactId, field, expected, actualValue, result ? "VRAI" : "FAUX");

        return result;
    }

    // ── Résolution des champs du Contact ─────────────────────────

    /**
     * Extrait la valeur textuelle du champ demandé depuis l'entité {@link Contact}.
     *
     * @param field   le nom du champ (ex: {@code "contact.email"})
     * @param contact l'entité Contact chargée depuis la base de données
     * @return la valeur du champ sous forme de chaîne, ou {@code null} si le champ est inconnu
     */
    private String resolveContactField(String field, Contact contact) {
        return switch (field) {
            case "contact.email"         -> contact.getEmail();
            case "contact.firstName"     -> contact.getFirstName();
            case "contact.lastName"      -> contact.getLastName();
            case "contact.city"          -> contact.getCity();
            case "contact.country"       -> contact.getCountry();
            case "contact.consentStatus" -> contact.getConsentStatus() != null
                    ? contact.getConsentStatus().name()
                    : null;
            default -> {
                log.warn("Champ de condition non reconnu : '{}'. Traité comme null.", field);
                yield null;
            }
        };
    }

    // ── Évaluation de l'appartenance à un segment ─────────────────

    /**
     * Vérifie si un contact appartient (ou non) à un segment donné.
     *
     * @param contactId   l'identifiant du contact (clé primaire Long)
     * @param segmentUuid l'UUID du segment cible
     * @param operator    {@code "IN_SEGMENT"} ou {@code "NOT_IN_SEGMENT"}
     * @return le résultat booléen de la condition
     */
    private boolean evaluateSegmentMembership(String contactId, String segmentUuid, String operator) {
        Contact contact = contactRepository.findById(Long.valueOf(contactId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contact introuvable pour l'évaluation de segment, id=" + contactId));

        UUID contactTrackingId = contact.getTrackingId();
        UUID segmentTrackingId;
        try {
            segmentTrackingId = UUID.fromString(segmentUuid);
        } catch (IllegalArgumentException e) {
            log.error("conditionValue '{}' n'est pas un UUID valide pour l'opérateur {}", segmentUuid, operator);
            return false;
        }

        boolean isMember = contactSegmentRepository
                .existsByContactTrackingIdAndSegmentTrackingId(contactTrackingId, segmentTrackingId);

        return "IN_SEGMENT".equals(operator) ? isMember : !isMember;
    }

    // ── Application de l'opérateur de comparaison ────────────────

    /**
     * Applique l'opérateur entre la valeur réelle et la valeur attendue.
     * Les comparaisons de chaînes sont insensibles à la casse.
     *
     * @param operator    l'opérateur de comparaison
     * @param actualValue la valeur réelle du champ (peut être null)
     * @param expected    la valeur de référence du nœud
     * @return le résultat de la comparaison
     */
    private boolean evaluateOperator(String operator, String actualValue, String expected) {
        return switch (operator) {
            case "EQUALS"       -> Objects.equals(
                    actualValue != null ? actualValue.toLowerCase() : null,
                    expected != null ? expected.toLowerCase() : null);
            case "NOT_EQUALS"   -> !Objects.equals(
                    actualValue != null ? actualValue.toLowerCase() : null,
                    expected != null ? expected.toLowerCase() : null);
            case "CONTAINS"     -> actualValue != null
                    && actualValue.toLowerCase().contains(expected.toLowerCase());
            case "STARTS_WITH"  -> actualValue != null
                    && actualValue.toLowerCase().startsWith(expected.toLowerCase());
            case "ENDS_WITH"    -> actualValue != null
                    && actualValue.toLowerCase().endsWith(expected.toLowerCase());
            case "IS_EMPTY"     -> actualValue == null || actualValue.isBlank();
            case "IS_NOT_EMPTY" -> actualValue != null && !actualValue.isBlank();
            default -> {
                log.warn("Opérateur de condition non reconnu : '{}'. Retourne false par défaut.", operator);
                yield false;
            }
        };
    }
}

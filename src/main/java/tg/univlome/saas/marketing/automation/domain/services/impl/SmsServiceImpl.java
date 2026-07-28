package tg.univlome.saas.marketing.automation.domain.services.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tg.univlome.saas.marketing.automation.domain.services.SmsService;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;

/**
 * Implémentation réelle du service d'envoi de SMS utilisant un client HTTP {@link WebClient}.
 *
 * <p>Compatible avec les fournisseurs d'API REST de SMS (Twilio, Brevo, Vonage, Africa's Talking...).</p>
 *
 * <p>Résilience et performances :</p>
 * <ul>
 *   <li>Format d'appel asynchrone "fire-and-forget" sans bloquer le moteur d'exécution.</li>
 *   <li>Timeout de requête configuré à 5 secondes.</li>
 *   <li>Les erreurs HTTP 4xx, 5xx ou réseau sont interceptées et consignées en niveau ERROR sans interrompre
 *       le traitement du worker RabbitMQ.</li>
 * </ul>
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private final WebClient webClient;
    private final ContactRepository contactRepository;

    @Value("${saas.sms.api-url}")
    private String apiUrl;

    @Value("${saas.sms.api-key:}")
    private String apiKey;

    @Value("${saas.sms.sender-id:SaaSPlatform}")
    private String senderId;

    public SmsServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
        this.webClient = WebClient.builder().build();
    }

    @Override
    public void sendSms(String recipientOrContactId, String messageBody) {
        if (recipientOrContactId == null || recipientOrContactId.isBlank()) {
            log.warn("[SMS SERVICE] Identifiant de contact ou numéro de téléphone vide. Abandon de l'envoi.");
            return;
        }

        // Résolution du numéro de téléphone (si un ID de contact est passé ou si c'est directement un numéro)
        String phoneNumber = resolvePhoneNumber(recipientOrContactId);

        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("[SMS SERVICE] Aucun numéro de téléphone disponible pour le destinataire [{}] (champ null/vide en BDD). "
                    + "Envoi ignoré.", recipientOrContactId);
            return;
        }

        if (apiUrl == null || apiUrl.isBlank()) {
            log.error("[SMS SERVICE] URL de l'API SMS non configurée (saas.sms.api-url). Envoi annulé.");
            return;
        }

        log.info("[SMS SERVICE] Préparation de l'envoi SMS vers [{}] (SenderId: '{}')", phoneNumber, senderId);

        // Construction d'un payload JSON générique compatible API REST SMS
        Map<String, Object> payload = new HashMap<>();
        payload.put("to", phoneNumber);
        payload.put("from", senderId);
        payload.put("text", messageBody != null ? messageBody : "");

        try {
            this.webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(5))
                    .doOnSuccess(response -> log.info(
                            "[SMS SERVICE] SMS envoyé avec succès vers [{}] — Status Code: {}",
                            phoneNumber, response.getStatusCode()))
                    .doOnError(error -> log.error(
                            "[SMS SERVICE] Échec de l'envoi du SMS vers [{}] — Cause: {}",
                            phoneNumber, error.getMessage()))
                    .subscribe(); // Exécution réactive asynchrone (Fire-and-Forget)

        } catch (Exception e) {
            log.error("[SMS SERVICE] Erreur inattendue lors de la soumission du SMS vers [{}] : {}",
                    phoneNumber, e.getMessage(), e);
        }
    }

    /**
     * Tente de résoudre un numéro de téléphone à partir de la chaîne passée en paramètre.
     * Si la chaîne correspond à un ID numérique de contact, recherche le contact en BDD.
     */
    private String resolvePhoneNumber(String input) {
        if (input.startsWith("+") || input.matches("^\\d{8,15}$")) {
            return input;
        }

        try {
            Long contactId = Long.parseLong(input);
            return contactRepository.findById(contactId)
                    .map(Contact::getPhone)
                    .orElse(null);
        } catch (NumberFormatException e) {
            log.debug("[SMS SERVICE] Entrée [{}] non numérique, traitée comme numéro brut.", input);
            return input;
        }
    }
}

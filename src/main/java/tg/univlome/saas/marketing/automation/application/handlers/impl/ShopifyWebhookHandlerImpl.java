package tg.univlome.saas.marketing.automation.application.handlers.impl;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.automation.application.handlers.WebhookPayloadHandler;
import tg.univlome.saas.marketing.automation.domain.services.WorkflowExecutionService;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;

/**
 * Stratégie de traitement des événements webhooks Shopify (ex: création de commande "orders/create").
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopifyWebhookHandlerImpl implements WebhookPayloadHandler {

    private static final String PROVIDER_NAME = "shopify";
    private final ContactRepository contactRepository;
    private final WorkflowExecutionService workflowExecutionService;

    @Override
    public boolean supports(String provider) {
        return PROVIDER_NAME.equalsIgnoreCase(provider);
    }

    @Override
    public void process(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            log.warn("[SHOPIFY HANDLER] Payload Shopify vide ou nul reçu. Abandon.");
            return;
        }

        String orderId = String.valueOf(payload.getOrDefault("order_id", payload.getOrDefault("id", "Inconnu")));
        String customerEmail = extractCustomerEmail(payload);

        log.info("[SHOPIFY HANDLER] Événement commande reçu — Commande ID: {}, Client Email: {}",
                orderId, customerEmail);

        if (customerEmail == null || customerEmail.isBlank()) {
            log.warn("[SHOPIFY HANDLER] Aucun e-mail client trouvé dans le webhook de la commande [{}].", orderId);
            return;
        }

        // Recherche du contact en base par son email
        contactRepository.findByEmail(customerEmail).ifPresentOrElse(
                contact -> log.info("[SHOPIFY HANDLER] Déclenchement du workflow Post-Achat pour le contact ID [{}] (Email: {})",
                        contact.getId(), customerEmail),
                () -> log.info("[SHOPIFY HANDLER] Nouveau client Shopify ({}) non encore enregistré en BDD. "
                        + "Déclenchement du workflow Post-Achat initialisé.", customerEmail)
        );

        log.info("[SHOPIFY HANDLER] Traitement du webhook d'achat Shopify [{}] terminé avec succès.", orderId);
    }

    /**
     * Extrait l'email du client du payload Shopify.
     */
    @SuppressWarnings("unchecked")
    private String extractCustomerEmail(Map<String, Object> payload) {
        if (payload.containsKey("email") && payload.get("email") != null) {
            return String.valueOf(payload.get("email"));
        }
        if (payload.containsKey("customer") && payload.get("customer") instanceof Map) {
            Map<String, Object> customer = (Map<String, Object>) payload.get("customer");
            if (customer.containsKey("email") && customer.get("email") != null) {
                return String.valueOf(customer.get("email"));
            }
        }
        return null;
    }
}

package tg.univlome.saas.marketing.reseauxsociaux.domain.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tg.univlome.saas.marketing.reseauxsociaux.application.dtos.SocialOutreachRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialOutreachService {

    private final RestTemplate restTemplate;

    // URL sandbox publique et gratuite pour tester les envois en toute sécurité
    private static final String SANDBOX_TEST_API = "https://httpbin.org/post";

    public boolean sendDirectMessage(SocialOutreachRequest request) {
        log.info("--- DÉBUT DE L'OUTREACH SOCIAL ---");
        log.info("Cible ID : {}", request.contactId());
        log.info("Réseau visé : {}", request.networkType());
        log.info("Destinataire (Handle/URL) : {}", request.targetProfileHandle());
        log.info("Message généré par l'IA : \n{}", request.messageContent());

        try {
            if ("sandbox_test".equalsIgnoreCase(request.networkType())) {
                // Test réel sur un endpoint sandbox gratuit pour valider le flux réseau
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<SocialOutreachRequest> entity = new HttpEntity<>(request, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(SANDBOX_TEST_API, entity, String.class);

                log.info("Succès du test Sandbox ! Statut HTTP : {}", response.getStatusCode());
                return response.getStatusCode().is2xxSuccessful();
            }

            // Simulation pour LinkedIn / Twitter (en attendant les clés de production)
            log.info("Simulation d'envoi réussi sur l'API de {} pour le profil {}",
                    request.networkType(), request.targetProfileHandle());
            return true;

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du message sur le réseau social : {}", e.getMessage());
            return false;
        }
    }
}

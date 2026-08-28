package tg.univlome.saas.marketing.ai.infrastructure.clients;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonAiPredictiveClient {

    private final RestTemplate restTemplate;

    // URL du microservice FastAPI en local
    private static final String PYTHON_API_URL = "http://localhost:8000/api/v1/predict/lead-score";

    public Map<String, Object> fetchLeadScoreFromPython(
            String contactId,
            int pagesVisited,
            int emailClicks,
            int timeOnSite,
            String recentMessage) {

        try {
            Map<String, Object> payload = Map.of(
                    "contact_id", contactId,
                    "pages_visited", pagesVisited,
                    "email_clicks", emailClicks,
                    "time_on_site", timeOnSite,
                    "recent_message", recentMessage != null ? recentMessage : ""
            );

            log.info("Appel du microservice Python FastAPI pour le contact {}...", contactId);
            ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_API_URL, payload, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Réponse reçue avec succès de Python : {}", response.getBody());
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Impossible de joindre le microservice Python sur le port 8000 : {}", e.getMessage());
        }

        // Fallback par défaut si Python est éteint
        return Map.of("score", 50.0, "risk_level", "UNKNOWN", "intent_analysis", "Erreur de liaison Python");
    }
}

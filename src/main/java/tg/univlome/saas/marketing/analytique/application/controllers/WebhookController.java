package tg.univlome.saas.marketing.analytique.application.controllers;


import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.analytique.application.dtos.request.SendGridEventRequest;
import tg.univlome.saas.marketing.analytique.domain.service.impl.AnalyticsService;

@Slf4j
@RestController
@RequestMapping("/api/webhooks/sendgrid")
@RequiredArgsConstructor
public class WebhookController {

    private final AnalyticsService analyticsService;

    // SendGrid utilise toujours la méthode POST
    @PostMapping
    public ResponseEntity<Void> receiveEvents(@RequestBody List<SendGridEventRequest> events) {
        log.info("Réception de {} événements depuis SendGrid", events.size());

        // On traite chaque événement dans la liste
        for (SendGridEventRequest event : events) {
            try {
                analyticsService.processSendGridEvent(event);
            } catch (Exception e) {
                // Si un événement pose problème, on le logge mais on ne bloque pas les autres
                log.error("Erreur lors du traitement de l'événement SendGrid", e);
            }
        }

        // Règle d'or des Webhooks : TOUJOURS répondre 200 OK le plus vite possible.
        // Sinon, SendGrid pensera que notre serveur est en panne et renverra les mêmes données en boucle.
        return ResponseEntity.ok().build();
    }
}

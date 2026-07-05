package tg.univlome.saas.marketing.email.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.email.application.dtos.requests.EmailMessage;
import tg.univlome.saas.marketing.email.domain.services.EmailService;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
@Tag(name = "Emails", description = "API de gestion et d'envoi d'emails (SendGrid)")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(
            summary = "Envoyer un email",
            description = "Prend en charge la demande d'envoi. Retourne un statut 202 (Accepted). "
                    + "Le traitement réel inclut un mécanisme de retry automatique en tâche de fond en cas d'instabilité réseau."
    )
    public ResponseEntity<String> sendEmail(@Valid @RequestBody EmailMessage request) {
        emailService.sendEmail(request);
        return ResponseEntity.accepted().body("L'email a été pris en charge et est en cours d'envoi.");
    }
}

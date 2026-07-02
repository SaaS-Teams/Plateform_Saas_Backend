package tg.univlome.saas.email.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.email.application.dtos.requests.EmailMessage;
import tg.univlome.saas.email.domain.services.EmailService;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
@Tag(name = "Emails", description = "API de gestion et d'envoi d'emails (SendGrid)")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(
            summary = "Envoyer un email",
            description = "Envoie un email via SendGrid, gère les tentatives en cas d'échec, et logue le résultat en base de données."
    )
    public ResponseEntity<String> sendEmail(@Valid @RequestBody EmailMessage request) {

        emailService.sendEmail(request);
        return ResponseEntity.accepted().body("L'email a été pris en charge et est en cours d'envoi.");
    }
}

package tg.univlome.saas.marketing.email.domain.services.impl;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.email.application.dtos.requests.EmailMessage;
import tg.univlome.saas.marketing.email.domain.enums.EmailStatus;
import tg.univlome.saas.marketing.email.domain.models.EmailLog;
import tg.univlome.saas.marketing.email.domain.services.EmailService;
import tg.univlome.saas.marketing.email.repositories.EmailLogRepository;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailLogRepository emailLogRepository;
    private final SendGridSender sendGridSender; // Notre nouveau sous-composant avec Retry

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${app.email.sender:contact@saas-marketing.tg}")
    private String senderEmail;

    // Validation au démarrage du SaaS (Fail-Fast demandé par Richard)
    @PostConstruct
    public void validateConfiguration() {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            throw new IllegalStateException("Configuration critique manquante : 'sendgrid.api-key' "
                    + "doit être renseignée dans l'environnement.");
        }
    }

    @Override
    public void sendEmail(EmailMessage message) {
        log.info("Préparation de l'envoi d'un email à {}", message.to());

        // 1. Initialisation et sauvegarde immédiate du statut PENDING
        EmailLog emailLog = new EmailLog();
        emailLog.setSender(senderEmail);
        emailLog.setRecipient(message.to());
        emailLog.setSubject(message.subject());
        emailLog.setStatus(EmailStatus.PENDING);

        emailLog = emailLogRepository.save(emailLog);

        try {
            // 2. Appel du composant isolé (gère les 3 tentatives en cas de coupure réseau)
            sendGridSender.sendViaSendGrid(sendGridApiKey, senderEmail, message, emailLog.getTrackingId());

            // 3. Si on arrive ici, c'est un succès complet
            emailLog.setStatus(EmailStatus.SENT);
            emailLog.setSentAt(LocalDateTime.now());
            emailLogRepository.save(emailLog);
            log.info("Email traité et envoyé avec succès à {}", message.to());

        } catch (IOException e) {
            // Déclenché uniquement après l'échec des 3 tentatives réseau distinctes
            log.error("Échec définitif d'envoi d'email à {} après épuisement des tentatives", message.to());

            emailLog.setStatus(EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            emailLogRepository.save(emailLog);

            // Message contextuel clair avec cause d'origine pour la production
            throw new RuntimeException("Impossible d'expédier l'email à destination de [" + message.to() + "] via SendGrid.", e);
        }
    }
}

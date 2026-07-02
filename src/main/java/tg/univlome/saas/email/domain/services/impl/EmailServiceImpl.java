package tg.univlome.saas.email.domain.services.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.email.application.dtos.requests.EmailMessage;
import tg.univlome.saas.email.domain.enums.EmailStatus;
import tg.univlome.saas.email.domain.models.EmailLog;
import tg.univlome.saas.email.domain.services.EmailService;
import tg.univlome.saas.email.repositories.EmailLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int BACKOFF_DELAY = 2000;
    private static final int HTTP_OK = 200;
    private static final int HTTP_ACCEPTED = 202;

    private final EmailLogRepository emailLogRepository;

    // Récupère la clé API depuis le fichier application.properties ou .env
    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${app.email.sender:contact@saas-marketing.tg}")
    private String senderEmail;

    @Override
    @Transactional
    @Retryable(
            value = { Exception.class },
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACKOFF_DELAY) // Attend 2 secondes entre chaque tentative
    )
    public void sendEmail(EmailMessage message) {
        log.info("Tentative d'envoi d'email à {}", message.to());

        // 1. On crée le log manuellement (Pas besoin de Mapper !)
        EmailLog emailLog = new EmailLog();
        emailLog.setSender(senderEmail);
        emailLog.setRecipient(message.to());
        emailLog.setSubject(message.subject());
        emailLog.setSentAt(LocalDateTime.now());
        emailLog.setStatus(EmailStatus.PENDING);

        emailLog = emailLogRepository.save(emailLog);

        try {
            // 2. Construction de l'email SendGrid
            Email from = new Email(senderEmail);
            Email to = new Email(message.to());
            Content content = new Content(
                    message.isHtml() ? "text/html" : "text/plain",
                    message.body()
            );
            Mail mail = new Mail(from, message.subject(), to, content);

            // 3. Préparation de la requête HTTP vers SendGrid
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            // 4. Exécution de la requête
            Response response = sg.api(request);

            if (response.getStatusCode() == HTTP_ACCEPTED || response.getStatusCode() == HTTP_OK) {
                emailLog.setStatus(EmailStatus.SENT);
                log.info("Email envoyé avec succès à {}", message.to());
            } else {
                throw new RuntimeException("Erreur API SendGrid : Code HTTP " + response.getStatusCode());
            }

        } catch (IOException | RuntimeException e) {
            log.error("Échec de l'envoi de l'email à {} : {}", message.to(), e.getMessage());

            // On met à jour le log avec l'erreur
            emailLog.setStatus(EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            emailLogRepository.save(emailLog);

            // On relance l'exception pour que l'annotation @Retryable de Spring déclenche un nouvel essai
            throw new RuntimeException("Échec de l'envoi, déclenchement du retry", e);
        }

        // 5. Sauvegarde finale en cas de succès
        emailLogRepository.save(emailLog);
    }
}

package tg.univlome.saas.marketing.email.domain.services.impl;


import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import tg.univlome.saas.marketing.email.application.dtos.requests.EmailMessage;


@Component
public class SendGridSender {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000L;
    private static final int HTTP_STATUS_OK = 200;
    private static final int HTTP_STATUS_ACCEPTED = 202;

    @Retryable(
            retryFor = { IOException.class },
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY_MS)
    )
    public void sendViaSendGrid(String apiKey, String senderEmail, EmailMessage message) throws IOException {
        Email from = new Email(senderEmail);
        Email to = new Email(message.to());
        Content content = new Content(message.isHtml() ? "text/html" : "text/plain", message.body());
        Mail mail = new Mail(from, message.subject(), to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        if (response.getStatusCode() != HTTP_STATUS_ACCEPTED && response.getStatusCode() != HTTP_STATUS_OK) {
            throw new IOException("L'API SendGrid a répondu avec un code d'erreur : " + response.getStatusCode());
        }
    }
}

package tg.univlome.saas.email.domain.services.impl;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tg.univlome.saas.email.application.dtos.requests.EmailMessage;
import tg.univlome.saas.email.domain.enums.EmailStatus;
import tg.univlome.saas.email.domain.models.EmailLog;
import tg.univlome.saas.email.repositories.EmailLogRepository;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailLogRepository emailLogRepository;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<EmailLog> emailLogCaptor;

    @BeforeEach
    void setUp() {
        // Injection manuelle de la valeur pour l'expéditeur et la clé API (comme @Value)
        ReflectionTestUtils.setField(emailService, "senderEmail", "contact@saas-marketing.tg");
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", "fake-api-key");
    }

    @Test
    void should_SendEmailSuccessfully_When_SendGridReturns202() throws IOException {
        // Arrange(GIVEN)
        EmailMessage message = new EmailMessage("test@example.com", "Sujet", "Corps du message", false);
        
        EmailLog savedLog = new EmailLog();
        savedLog.setStatus(EmailStatus.PENDING);
        
        // Mock du repository pour qu'il retourne le log lors du premier save()
        when(emailLogRepository.save(any(EmailLog.class))).thenReturn(savedLog);

        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);

        // Mock de l'instanciation de SendGrid (car c'est un "new SendGrid()" dans le service)
        try (MockedConstruction<SendGrid> mockedSendGrid = Mockito.mockConstruction(SendGrid.class,
                (mock, context) -> {
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act(WHEN)
            emailService.sendEmail(message);

            // Assert(THEN)
            verify(emailLogRepository, times(2)).save(emailLogCaptor.capture());
            
            // Premier save (PENDING)
            EmailLog firstSave = emailLogCaptor.getAllValues().get(0);
            assertThat(firstSave.getStatus()).isEqualTo(EmailStatus.PENDING);
            assertThat(firstSave.getRecipient()).isEqualTo("test@example.com");
            
            // Deuxième save (SENT)
            EmailLog secondSave = emailLogCaptor.getAllValues().get(1);
            assertThat(secondSave.getStatus()).isEqualTo(EmailStatus.SENT);
        }
    }

    @Test
    void should_ThrowExceptionAndLogAsFailed_When_SendGridReturns400() throws IOException {
        // Arrange(GIVEN)
        EmailMessage message = new EmailMessage("test@example.com", "Sujet", "Corps du message", false);
        
        EmailLog savedLog = new EmailLog();
        savedLog.setStatus(EmailStatus.PENDING);
        
        when(emailLogRepository.save(any(EmailLog.class))).thenReturn(savedLog);

        Response mockResponse = new Response();
        mockResponse.setStatusCode(400);

        try (MockedConstruction<SendGrid> mockedSendGrid = Mockito.mockConstruction(SendGrid.class,
                (mock, context) -> {
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // Act(WHEN) & Assert(THEN)
            assertThatThrownBy(() -> emailService.sendEmail(message))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Échec de l'envoi, déclenchement du retry")
                    .hasCauseInstanceOf(RuntimeException.class);
            
            // Assert(THEN) (Vérification des logs)
            // On vérifie qu'il y a eu un premier save (PENDING) et un deuxième (FAILED) suite à l'erreur
            verify(emailLogRepository, times(2)).save(emailLogCaptor.capture());
            
            EmailLog failedSave = emailLogCaptor.getAllValues().get(1);
            assertThat(failedSave.getStatus()).isEqualTo(EmailStatus.FAILED);
            assertThat(failedSave.getErrorMessage()).contains("Code HTTP 400");
        }
    }
}

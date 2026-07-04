package tg.univlome.saas.email.domain.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tg.univlome.saas.email.application.dtos.requests.EmailMessage;
import tg.univlome.saas.email.domain.enums.EmailStatus;
import tg.univlome.saas.email.domain.models.EmailLog;
import tg.univlome.saas.email.domain.services.impl.EmailServiceImpl;
import tg.univlome.saas.email.domain.services.impl.SendGridSender;
import tg.univlome.saas.email.repositories.EmailLogRepository;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private SendGridSender sendGridSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<EmailLog> emailLogCaptor;

    private EmailMessage testMessage;

    @BeforeEach
    void setUp() {
        // Injection manuelle des variables @Value pour le test
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "senderEmail", "test@saas.tg");

        testMessage = new EmailMessage("client@domaine.com", "Sujet Test", "Contenu Test", false);
    }

    @Test
    void should_ThrowException_When_ApiKeyIsMissing() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", ""); // Clé vide

        // Act & Assert
        assertThatThrownBy(() -> emailService.validateConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Configuration critique manquante");
    }

    @Test
    void should_SendEmail_And_SaveLog_When_Success() throws IOException {
        // Arrange
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(invocation -> {
            EmailLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        // Act
        emailService.sendEmail(testMessage);

        // Assert
        // Vérifie que le composant SendGridSender a bien été appelé
        verify(sendGridSender, times(1)).sendViaSendGrid("test-api-key", "test@saas.tg", testMessage);

        // Vérifie que le repository a sauvegardé le log 2 fois (PENDING puis SENT)
        verify(emailLogRepository, times(2)).save(emailLogCaptor.capture());

        EmailLog finalLog = emailLogCaptor.getAllValues().get(1);
        assertThat(finalLog.getStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(finalLog.getSentAt()).isNotNull();
    }

    @Test
    void should_ThrowException_And_SaveFailedLog_When_SendGridFails() throws IOException {
        // Arrange
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // On simule une erreur réseau du composant SendGridSender
        doThrow(new IOException("Timeout API")).when(sendGridSender)
                .sendViaSendGrid(anyString(), anyString(), any(EmailMessage.class));

        // Act & Assert
        assertThatThrownBy(() -> emailService.sendEmail(testMessage))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Impossible d'expédier l'email");

        // Vérifie que le repository a sauvegardé le log final en FAILED
        verify(emailLogRepository, times(2)).save(emailLogCaptor.capture());

        EmailLog failedLog = emailLogCaptor.getAllValues().get(1);
        assertThat(failedLog.getStatus()).isEqualTo(EmailStatus.FAILED);
        assertThat(failedLog.getErrorMessage()).contains("Timeout API");
        assertThat(failedLog.getSentAt()).isNull(); // Ne doit pas avoir de date d'envoi
    }
}

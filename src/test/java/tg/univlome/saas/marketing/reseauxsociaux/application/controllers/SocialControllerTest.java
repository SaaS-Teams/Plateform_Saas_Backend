package tg.univlome.saas.marketing.reseauxsociaux.application.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialAccount;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPostPayload;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPublishResult;
import tg.univlome.saas.marketing.reseauxsociaux.domain.services.SocialPublishService;
import tg.univlome.saas.marketing.reseauxsociaux.repositories.SocialAccountRepository;
import tg.univlome.saas.shared.security.crypto.CryptoService;

@ExtendWith(MockitoExtension.class)
class SocialControllerTest {

    @Mock
    private SocialPublishService publishService;

    @Mock
    private SocialAccountRepository accountRepository;

    @Mock
    private CryptoService cryptoService;

    @InjectMocks
    private SocialController socialController;

    @Test
    void linkAccount_ShouldEncryptTokensAndSaveAccount() {
        Long userId = 1L;
        UUID workspaceUuid = UUID.randomUUID();
        String platformKey = "bluesky";
        String plainAccessToken = "raw-access-token";
        String plainRefreshToken = "raw-refresh-token";

        when(cryptoService.encrypt(plainAccessToken)).thenReturn("ENCRYPTED_ACCESS");
        when(cryptoService.encrypt(plainRefreshToken)).thenReturn("ENCRYPTED_REFRESH");
        when(accountRepository.save(any(SocialAccount.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<SocialAccount> response = socialController.linkAccount(
                userId, workspaceUuid, platformKey, plainAccessToken, plainRefreshToken, "@user_handle");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isEqualTo("ENCRYPTED_ACCESS");
        assertThat(response.getBody().getRefreshToken()).isEqualTo("ENCRYPTED_REFRESH");
        assertThat(response.getBody().getPlatformKey()).isEqualTo("bluesky");
    }

    @Test
    void publishContent_ShouldDelegateToPublishService() {
        Long userId = 1L;
        SocialPostPayload payload = new SocialPostPayload("contact-1", "bluesky", "@handle", "Message");
        SocialPublishResult mockResult = SocialPublishResult.ok("ext-id-123");

        when(publishService.publishPost(eq(userId), any(SocialPostPayload.class))).thenReturn(mockResult);

        ResponseEntity<SocialPublishResult> response = socialController.publishContent(userId, payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().platformResponseId()).isEqualTo("ext-id-123");
    }
}

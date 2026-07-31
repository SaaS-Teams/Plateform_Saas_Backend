package tg.univlome.saas.marketing.reseauxsociaux.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialAccount;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPostPayload;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPublishResult;
import tg.univlome.saas.marketing.reseauxsociaux.domain.ports.SocialPlatformPort;
import tg.univlome.saas.marketing.reseauxsociaux.repositories.SocialAccountRepository;
import tg.univlome.saas.shared.security.crypto.CryptoService;

@ExtendWith(MockitoExtension.class)
class SocialPublishServiceTest {

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private SocialPlatformPort mockAdapter;

    @Spy
    private List<SocialPlatformPort> platformAdapters = List.of();

    @InjectMocks
    private SocialPublishService socialPublishService;

    @Test
    void publishPost_ShouldDecryptTokenAndDelegateToMatchingAdapter() {
        // Arrange
        Long userId = 1L;
        String platformKey = "sandbox_test";
        SocialPostPayload payload = new SocialPostPayload("contact-1", platformKey, "@test", "Hello World");

        SocialAccount account = SocialAccount.builder()
                .id(10L)
                .userId(userId)
                .platformKey(platformKey)
                .accessToken("ENCRYPTED_TOKEN")
                .build();

        SocialPublishService serviceWithAdapters = new SocialPublishService(
                List.of(mockAdapter),
                socialAccountRepository,
                cryptoService
        );

        when(socialAccountRepository.findByUserIdAndPlatformKey(userId, platformKey))
                .thenReturn(Optional.of(account));
        when(cryptoService.decrypt("ENCRYPTED_TOKEN")).thenReturn("RAW_DECRYPTED_TOKEN");
        when(mockAdapter.supports(platformKey)).thenReturn(true);
        when(mockAdapter.publish(any(SocialAccount.class), any(SocialPostPayload.class)))
                .thenReturn(SocialPublishResult.ok("external-123"));

        // Act
        SocialPublishResult result = serviceWithAdapters.publishPost(userId, payload);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.platformResponseId()).isEqualTo("external-123");
    }
}

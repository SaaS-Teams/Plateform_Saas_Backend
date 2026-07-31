package tg.univlome.saas.marketing.reseauxsociaux.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import tg.univlome.saas.marketing.reseauxsociaux.application.dtos.SocialOutreachRequest;

@ExtendWith(MockitoExtension.class)
class SocialOutreachServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SocialOutreachService socialOutreachService;

    @Test
    void sendDirectMessage_ShouldReturnTrueForSandboxTestWhenApiIs2xx() {
        SocialOutreachRequest request = new SocialOutreachRequest(
                "c-100",
                "sandbox_test",
                "@john_doe",
                "Hello from AI"
        );

        when(restTemplate.postForEntity(eq("https://httpbin.org/post"), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        boolean result = socialOutreachService.sendDirectMessage(request);
        assertThat(result).isTrue();
    }

    @Test
    void sendDirectMessage_ShouldReturnTrueForLinkedInSimulation() {
        SocialOutreachRequest request = new SocialOutreachRequest(
                "c-101",
                "linkedin",
                "linkedin.com/in/johndoe",
                "Hello LinkedIn"
        );

        boolean result = socialOutreachService.sendDirectMessage(request);
        assertThat(result).isTrue();
    }
}

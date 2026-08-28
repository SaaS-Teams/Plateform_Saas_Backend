package tg.univlome.saas.marketing.reseauxsociaux.domain.adapters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialAccount;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPostPayload;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPublishResult;
import tg.univlome.saas.marketing.reseauxsociaux.domain.ports.SocialPlatformPort;

@Slf4j
@Component
@RequiredArgsConstructor
public class SandboxTestPlatformAdapter implements SocialPlatformPort {

    private final RestTemplate restTemplate;
    private static final String SANDBOX_URL = "https://httpbin.org/post";

    @Override
    public boolean supports(String platformKey) {
        return "sandbox_test".equalsIgnoreCase(platformKey);
    }

    @Override
    public SocialPublishResult publish(SocialAccount account, SocialPostPayload payload) {
        log.info("[SANDBOX ADAPTER] Publication sur la sandbox pour l'utilisateur ID: {}", account.getUserId());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(account.getAccessToken());

            HttpEntity<SocialPostPayload> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(SANDBOX_URL, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[SANDBOX ADAPTER] Publication réussie ! HTTP Status: {}", response.getStatusCode());
                return SocialPublishResult.ok("sandbox-msg-" + System.currentTimeMillis());
            } else {
                return SocialPublishResult.fail("Erreur HTTP Sandbox: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("[SANDBOX ADAPTER] Échec de la publication sandbox: {}", e.getMessage());
            return SocialPublishResult.fail("Exception Sandbox: " + e.getMessage());
        }
    }
}

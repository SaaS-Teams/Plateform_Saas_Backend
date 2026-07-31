package tg.univlome.saas.marketing.reseauxsociaux.application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialAccount;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPostPayload;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPublishResult;
import tg.univlome.saas.marketing.reseauxsociaux.domain.services.SocialPublishService;
import tg.univlome.saas.marketing.reseauxsociaux.repositories.SocialAccountRepository;
import tg.univlome.saas.shared.security.crypto.CryptoService;

@Slf4j
@RestController
@RequestMapping("/api/v1/social")
@RequiredArgsConstructor
@Tag(name = "Social Networks", description = "Endpoints de gestion des comptes sociaux et publication d'outreach")
public class SocialController {

    private final SocialPublishService publishService;
    private final SocialAccountRepository accountRepository;
    private final CryptoService cryptoService;

    @PostMapping("/accounts")
    @Operation(summary = "Lier un nouveau compte social avec chiffrement des tokens")
    public ResponseEntity<SocialAccount> linkAccount(
            @RequestParam Long userId,
            @RequestParam UUID workspaceTrackingId,
            @RequestParam String platformKey,
            @RequestParam String plainAccessToken,
            @RequestParam(required = false) String plainRefreshToken,
            @RequestParam(required = false) String accountHandle
    ) {
        log.info("[SOCIAL CONTROLLER] Lier un compte pour userId: {}, platformKey: {}", userId, platformKey);

        SocialAccount account = SocialAccount.builder()
                .userId(userId)
                .workspaceTrackingId(workspaceTrackingId)
                .platformKey(platformKey)
                .accountHandle(accountHandle)
                .accessToken(cryptoService.encrypt(plainAccessToken))
                .refreshToken(plainRefreshToken != null ? cryptoService.encrypt(plainRefreshToken) : null)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        SocialAccount saved = accountRepository.save(account);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/publish")
    @Operation(summary = "Publier un contenu sur un réseau social ciblé")
    public ResponseEntity<SocialPublishResult> publishContent(
            @RequestParam Long userId,
            @RequestBody SocialPostPayload payload
    ) {
        log.info("[SOCIAL CONTROLLER] Demande de publication reçue pour userId: {}, platformKey: {}",
                userId, payload.platformKey());
        SocialPublishResult result = publishService.publishPost(userId, payload);
        return ResponseEntity.ok(result);
    }
}

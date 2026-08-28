package tg.univlome.saas.marketing.reseauxsociaux.domain.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialAccount;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPostPayload;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPublishResult;
import tg.univlome.saas.marketing.reseauxsociaux.domain.ports.SocialPlatformPort;
import tg.univlome.saas.marketing.reseauxsociaux.repositories.SocialAccountRepository;
import tg.univlome.saas.shared.exceptions.ResourceNotFoundException;
import tg.univlome.saas.shared.security.crypto.CryptoService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialPublishService {

    private final List<SocialPlatformPort> platformAdapters;
    private final SocialAccountRepository socialAccountRepository;
    private final CryptoService cryptoService;

    /**
     * Publie un message sur la plateforme ciblée via son adaptateur dédié.
     * Déchiffre le jeton d'accès uniquement en mémoire pour l'appel API sortant.
     *
     * @param userId l'identifiant de l'utilisateur propriétaire du compte
     * @param payload le contenu du post à publier
     * @return le résultat de la publication
     */
    public SocialPublishResult publishPost(Long userId, SocialPostPayload payload) {
        log.info("[SOCIAL PUBLISH SERVICE] Publication demandée par userId: [{}] sur plateforme: [{}]",
                userId, payload.platformKey());

        SocialAccount account = socialAccountRepository.findByUserIdAndPlatformKey(userId, payload.platformKey())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun compte lié trouvé pour l'utilisateur " + userId + " sur la plateforme " + payload.platformKey()));

        // Déchiffrement temporaire du jeton d'accès en mémoire pour l'appel API
        String decryptedAccessToken = cryptoService.decrypt(account.getAccessToken());

        // Création d'une instance transitoire contenant le token en clair pour l'adaptateur
        SocialAccount transientAccount = SocialAccount.builder()
                .id(account.getId())
                .trackingId(account.getTrackingId())
                .userId(account.getUserId())
                .workspaceTrackingId(account.getWorkspaceTrackingId())
                .platformKey(account.getPlatformKey())
                .accountHandle(account.getAccountHandle())
                .accessToken(decryptedAccessToken)
                .refreshToken(account.getRefreshToken())
                .expiresAt(account.getExpiresAt())
                .build();

        // Sélection dynamique de l'adaptateur via le pattern Strategy / Spring Registry
        SocialPlatformPort adapter = platformAdapters.stream()
                .filter(a -> a.supports(payload.platformKey()))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException(
                        "Aucun adaptateur disponible pour la plateforme : " + payload.platformKey()));

        return adapter.publish(transientAccount, payload);
    }
}

package tg.univlome.saas.marketing.reseauxsociaux.domain.models;

import java.time.LocalDateTime;

public record SocialPublishResult(
        boolean success,
        String platformResponseId,
        String errorMessage,
        LocalDateTime publishedAt
) {
    public static SocialPublishResult ok(String platformResponseId) {
        return new SocialPublishResult(true, platformResponseId, null, LocalDateTime.now());
    }

    public static SocialPublishResult fail(String errorMessage) {
        return new SocialPublishResult(false, null, errorMessage, LocalDateTime.now());
    }
}

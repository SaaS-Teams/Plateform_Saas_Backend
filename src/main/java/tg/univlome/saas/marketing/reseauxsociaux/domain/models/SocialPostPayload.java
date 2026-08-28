package tg.univlome.saas.marketing.reseauxsociaux.domain.models;

public record SocialPostPayload(
        String contactId,
        String platformKey,
        String targetProfileHandle,
        String messageContent
) {
}

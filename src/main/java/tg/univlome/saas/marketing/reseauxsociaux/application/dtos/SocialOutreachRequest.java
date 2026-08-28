package tg.univlome.saas.marketing.reseauxsociaux.application.dtos;

public record SocialOutreachRequest(
        String contactId,
        String networkType,
        String targetProfileHandle,
        String messageContent
) {
}

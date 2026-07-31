package tg.univlome.saas.marketing.reseauxsociaux.application.dtos;

import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialNetworkType;

public record SocialOutreachRequest(
        String contactId,
        SocialNetworkType networkType,
        String targetProfileHandle,
        String messageContent
) {
}

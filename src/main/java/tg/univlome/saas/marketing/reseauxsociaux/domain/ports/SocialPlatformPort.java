package tg.univlome.saas.marketing.reseauxsociaux.domain.ports;

import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialAccount;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPostPayload;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialPublishResult;

/**
 * Contrat d'adaptateur pour les plateformes de réseaux sociaux (Pattern Strategy).
 * Permet l'ajout dynamique de nouveaux réseaux sociaux via platformKey sans modifier le code existant.
 */
public interface SocialPlatformPort {

    /**
     * Vérifie si cet adaptateur prend en charge la clé de plateforme donnée.
     *
     * @param platformKey la clé textuelle de la plateforme (ex: "bluesky", "tiktok", "facebook")
     * @return true si l'adaptateur gère cette plateforme, false sinon
     */
    boolean supports(String platformKey);

    /**
     * Publie un message/post sur la plateforme ciblée en utilisant le compte authentifié.
     *
     * @param account le compte social contenant le jeton déchiffré
     * @param payload le contenu et destinataire du post à publier
     * @return le résultat de la publication
     */
    SocialPublishResult publish(SocialAccount account, SocialPostPayload payload);
}

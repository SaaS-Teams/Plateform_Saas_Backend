package tg.univlome.saas.marketing.automation.domain.services;

/**
 * Contrat du service d'envoi de SMS dans le moteur d'automation.
 *
 * <p>Les implémentations futures pourront s'appuyer sur des fournisseurs
 * tiers tels que Twilio, Orange SMS API ou Africa's Talking.</p>
 */
public interface SmsService {

    /**
     * Envoie un SMS à un contact identifié par son numéro de téléphone.
     *
     * @param phoneNumber le numéro de téléphone du destinataire (format international, ex: +22890000000)
     * @param messageBody le corps du message à envoyer
     */
    void sendSms(String phoneNumber, String messageBody);
}

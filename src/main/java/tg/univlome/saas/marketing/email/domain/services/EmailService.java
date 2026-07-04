package tg.univlome.saas.marketing.email.domain.services;

import tg.univlome.saas.marketing.email.application.dtos.requests.EmailMessage;

public interface EmailService {
    /**
     * Envoie un email en utilisant SendGrid ou MailHog selon l'environnement.
     * Gère automatiquement 3 tentatives en cas d'échec et logue l'envoi.
     *
     * @param message Les informations de l'email à envoyer
     */

    void sendEmail(EmailMessage message);

}


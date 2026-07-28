package tg.univlome.saas.marketing.automation.domain.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tg.univlome.saas.marketing.automation.domain.services.SmsService;

/**
 * Implémentation de démonstration du service SMS.
 *
 * <p>Log l'envoi de SMS en attendant l'intégration d'un provider réel (Twilio, Africa's Talking...).</p>
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendSms(String phoneNumber, String messageBody) {
        log.info("[SMS SERVICE] Envoi de SMS à [{}] — Message : '{}'", phoneNumber, messageBody);
    }
}

package tg.univlome.saas.marketing.contact.domain.services;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.univlome.saas.marketing.contact.application.dtos.response.ConsentLogResponse;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;
import tg.univlome.saas.marketing.contact.domain.models.Contact;

public interface ConsentLogService {

    // Méthode interne pour les autres services
    void recordLog(Contact contact, ConsentAction action, String ipAddress);

    // Méthode publique pour l'API
    Page<ConsentLogResponse> getContactConsentHistory(UUID contactTrackingId, Pageable pageable);
}

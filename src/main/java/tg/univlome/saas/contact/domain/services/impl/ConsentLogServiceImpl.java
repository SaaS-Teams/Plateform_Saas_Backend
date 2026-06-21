package tg.univlome.saas.marketing.contact.domain.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.contact.application.dtos.response.ConsentLogResponse;
import tg.univlome.saas.marketing.contact.application.mappers.ConsentLogMapper;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;
import tg.univlome.saas.marketing.contact.domain.models.ConsentLog;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.domain.services.ConsentLogService;
import tg.univlome.saas.marketing.contact.repositories.ConsentLogRepository;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsentLogServiceImpl implements ConsentLogService {

    private final ConsentLogRepository consentLogRepository;
    private final ContactRepository contactRepository;
    private final ConsentLogMapper consentLogMapper;

    @Override
    @Transactional
    public void recordLog(Contact contact, ConsentAction action, String ipAddress) {
        ConsentLog log = new ConsentLog();
        log.setAction(action);
        log.setContact(contact);
        log.setIpAddress(ipAddress);
        consentLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsentLogResponse> getContactConsentHistory(UUID contactTrackingId, Pageable pageable) {
        // Vérifie si le contact existe avant de chercher ses logs
        if (!contactRepository.findByTrackingId(contactTrackingId).isPresent()) {
            throw new IllegalArgumentException("Contact introuvable avec l'ID : " + contactTrackingId);
        }

        Page<ConsentLog> logsPage = consentLogRepository.findByContact_TrackingId(contactTrackingId, pageable);
        return logsPage.map(consentLogMapper::toResponse); // Utilise la pagination native de Spring avec notre Mapper
    }
}

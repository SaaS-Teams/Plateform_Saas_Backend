package tg.univlome.saas.marketing.contact.domain.services.impl;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tg.univlome.saas.marketing.contact.application.dtos.request.ContactRequest;
import tg.univlome.saas.marketing.contact.application.dtos.response.ContactResponse;
import tg.univlome.saas.marketing.contact.application.dtos.response.ImportResult;
import tg.univlome.saas.marketing.contact.application.mappers.ContactMapper;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;
import tg.univlome.saas.marketing.contact.domain.models.Contact;
import tg.univlome.saas.marketing.contact.domain.services.ConsentLogService;
import tg.univlome.saas.marketing.contact.domain.services.ContactService;
import tg.univlome.saas.marketing.contact.repositories.ContactRepository;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final ConsentLogService consentLogService;

    /**
     * Crée un nouveau contact pour la plateforme marketing.
     *
     * @param request  les données du contact à créer
     * @param ipAddress l'adresse IP du client pour l'enregistrement du consentement
     * @return le contact créé sous forme de DTO
     * @throws IllegalArgumentException si un contact avec le même email existe déjà
     */
    @Override
    @Transactional
    public ContactResponse createContact(ContactRequest request, String ipAddress) {
        // 1. Règle métier : Déduplication
        if (contactRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Tentative de création d'un contact avec un email existant : {}", request.email());
            throw new IllegalArgumentException("Un contact avec cet email existe déjà.");
        }

        // 2. Création
        Contact contact = contactMapper.toEntity(request);
        contact = contactRepository.save(contact);

        // 3. Log RGPD automatique (Initialisé en PENDING par défaut dans l'entité)
        consentLogService.recordLog(contact, ConsentAction.GRANTED, ipAddress);
        log.info("Contact créé avec succès : id={}, email={}", contact.getId(), contact.getEmail());

        return contactMapper.toResponse(contact);
    }

    /**
     * Met à jour les informations d'un contact existant.
     *
     * @param trackingId l'identifiant de suivi du contact
     * @param request les nouvelles données du contact
     * @return le contact mis à jour sous forme de DTO
     * @throws IllegalArgumentException si le contact n'existe pas ou si l'email est déjà utilisé
     */
    @Override
    @Transactional
    public ContactResponse updateContact(UUID trackingId, ContactRequest request) {
        Contact contact = contactRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> {
                    log.error("Tentative de mise à jour d'un contact introuvable : {}", trackingId);
                    return new IllegalArgumentException("Contact introuvable");
                });

        // On empêche la modification de l'email si un autre compte l'utilise déjà
        if (!contact.getEmail().equals(request.email()) && contactRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Tentative d'utilisation d'un email déjà pris : {}", request.email());
            throw new IllegalArgumentException("Cet email est déjà pris par un autre contact.");
        }

        contactMapper.updateEntityFromRequest(request, contact);
        contact = contactRepository.save(contact);
        log.info("Contact mis à jour avec succès : trackingId={}", trackingId);

        return contactMapper.toResponse(contact);
    }

    /**
     * Récupère un contact par son identifiant de suivi.
     *
     * @param trackingId l'identifiant de suivi du contact
     * @return le contact sous forme de DTO
     * @throws IllegalArgumentException si le contact n'existe pas
     */
    @Override
    @Transactional(readOnly = true)
    public ContactResponse getContactByTrackingId(UUID trackingId) {
        Contact contact = contactRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> {
                    log.error("Contact introuvable lors de la recherche : {}", trackingId);
                    return new IllegalArgumentException("Contact introuvable");
                });
        return contactMapper.toResponse(contact);
    }

    /**
     * Récupère la liste de tous les contacts de manière paginée.
     *
     * @param pageable les informations de pagination
     * @return une page de contacts sous forme de DTO
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> getAllContacts(Pageable pageable) {
        return contactRepository.findAll(pageable)
                .map(contactMapper::toResponse); // Pagination native map
    }

    /**
     * Modifie le statut de consentement d'un contact.
     *
     * @param trackingId l'identifiant de suivi du contact
     * @param newStatus le nouveau statut de consentement
     * @param ipAddress l'adresse IP du client pour l'enregistrement du journal
     * @return le contact avec le statut mis à jour sous forme de DTO
     * @throws IllegalArgumentException si le contact n'existe pas
     */
    @Override
    @Transactional
    public ContactResponse changeConsentStatus(UUID trackingId, ConsentStatus newStatus, String ipAddress) {
        Contact contact = contactRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> {
                    log.error("Contact introuvable pour modification du consentement : {}", trackingId);
                    return new IllegalArgumentException("Contact introuvable");
                });

        if (contact.getConsentStatus() != newStatus) {
            contact.setConsentStatus(newStatus);
            contact = contactRepository.save(contact);

            ConsentAction action = (newStatus == ConsentStatus.OPT_IN) ? ConsentAction.GRANTED : ConsentAction.REVOKED;
            consentLogService.recordLog(contact, action, ipAddress);
            log.info("Statut de consentement mis à jour : trackingId={}, nouveauStatut={}", trackingId, newStatus);
        }

        return contactMapper.toResponse(contact);
    }

    /**
     * Importe une liste de contacts à partir d'un fichier CSV.
     *
     * @param file le fichier CSV contenant les contacts
     * @return un résumé de l'import (total, importés, ignorés, erreurs)
     * @throws RuntimeException en cas d'erreur de lecture du fichier
     */
    @Override
    public ImportResult importContactsFromCsv(MultipartFile file) {
        int total = 0;
        int imported = 0;
        int ignored = 0;
        int errors = 0;

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {

            List<String[]> rows = csvReader.readAll();
            total = rows.size();

            for (String[] row : rows) {
                // Format attendu du CSV : email, firstName, lastName, city, country
                if (row.length < 1 || row[0].trim().isEmpty()) {
                    errors++;
                    continue;
                }

                String email = row[0].trim();

                // On ignore les doublons
                if (contactRepository.existsByEmail(email)) {
                    ignored++;
                    continue;
                }

                if (processCsvRowAndSave(row, email)) {
                    imported++;
                } else {
                    errors++;
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de la lecture du fichier CSV d'importation", e);
            throw new RuntimeException("Erreur lors de la lecture du fichier CSV : " + e.getMessage());
        }

        log.info("Importation CSV terminée. Total: {}, Importés: {}, Ignorés: {}, Erreurs: {}",
                total, imported, ignored, errors);
        return new ImportResult(total, imported, ignored, errors);
    }

    private boolean processCsvRowAndSave(String[] row, String email) {
        final int cityIndex = 3;
        final int countryIndex = 4;

        try {
            Contact contact = new Contact();
            contact.setEmail(email);
            if (row.length > 1) {
                contact.setFirstName(row[1].trim());
            }
            if (row.length > 2) {
                contact.setLastName(row[2].trim());
            }
            if (row.length > cityIndex) {
                contact.setCity(row[cityIndex].trim());
            }
            if (row.length > countryIndex) {
                contact.setCountry(row[countryIndex].trim());
            }

            contact.setConsentStatus(ConsentStatus.PENDING);
            contactRepository.save(contact);
            return true;
        } catch (Exception e) {
            log.warn("Erreur lors de l'enregistrement du contact importé (email={})", email, e);
            return false;
        }
    }
}

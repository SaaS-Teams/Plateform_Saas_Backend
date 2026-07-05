package tg.univlome.saas.marketing.campagne.domain.services.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.univlome.saas.marketing.campagne.application.dtos.request.CampagneRequest;
import tg.univlome.saas.marketing.campagne.application.dtos.response.CampagneResponse;
import tg.univlome.saas.marketing.campagne.application.mappers.CampagneMapper;
import tg.univlome.saas.marketing.campagne.domain.models.Campagne;
import tg.univlome.saas.marketing.campagne.domain.services.CampagneService;
import tg.univlome.saas.marketing.campagne.repositories.CampagneRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampagneServiceImpl implements CampagneService {

    private final CampagneRepository campagneRepository;
    private final CampagneMapper campagneMapper;

    @Override
    @Transactional
    public CampagneResponse createCampagne(CampagneRequest request) {
        log.info("Création d'une nouvelle campagne : {}", request.nom());

        Campagne campagne = campagneMapper.toEntity(request);
        Campagne savedCampagne = campagneRepository.save(campagne);

        return campagneMapper.toResponse(savedCampagne);
    }



    @Override
    @Transactional(readOnly = true)
    public CampagneResponse getCampagneByTrackingId(UUID trackingId) {
        log.info("Récupération de la campagne avec le Tracking ID : {}", trackingId);

        Campagne campagne = campagneRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable avec le Tracking ID : " + trackingId));

        return campagneMapper.toResponse(campagne);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampagneResponse> getAllCampagnes() {
        log.info("Récupération de toutes les campagnes");

        return campagneRepository.findAll().stream()
                .map(campagneMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CampagneResponse updateCampagne(UUID trackingId, CampagneRequest request) {
        log.info("Mise à jour de la campagne avec l'ID : {}", trackingId);

        Campagne campagne = campagneRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable avec l'ID : " + trackingId));

        // On met à jour l'entité existante grâce à notre mapper
        campagneMapper.updateEntityFromRequest(campagne, request);

        Campagne updatedCampagne = campagneRepository.save(campagne);
        return campagneMapper.toResponse(updatedCampagne);
    }

    public void deleteCampagne(UUID trackingId) {
        log.info("Suppression de la campagne avec l'ID : {}", trackingId);

        Campagne campagne = campagneRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable avec l'ID : " + trackingId));

        campagneRepository.delete(campagne);
    }

}

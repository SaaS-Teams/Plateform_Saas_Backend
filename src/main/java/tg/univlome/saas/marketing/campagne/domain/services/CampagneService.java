package tg.univlome.saas.marketing.campagne.domain.services;

import tg.univlome.saas.marketing.campagne.application.dtos.request.CampagneRequest;
import tg.univlome.saas.marketing.campagne.application.dtos.response.CampagneResponse;

import java.util.List;
import java.util.UUID;

public interface CampagneService {
    CampagneResponse createCampagne(CampagneRequest request);
    CampagneResponse getCampagneByTrackingId(UUID trackingId);
    List<CampagneResponse> getAllCampagnes();
    CampagneResponse updateCampagne(UUID trackingId, CampagneRequest request);
    void deleteCampagne(UUID trackingId);
}

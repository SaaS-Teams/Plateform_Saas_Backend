package tg.univlome.saas.marketing.campagne.application.mappers;

import org.springframework.stereotype.Component;


import tg.univlome.saas.marketing.campagne.application.dtos.response.CampagneResponse;
import tg.univlome.saas.marketing.campagne.application.dtos.request.CampagneRequest;
import tg.univlome.saas.marketing.campagne.domain.enums.CampagneStatus;
import tg.univlome.saas.marketing.campagne.domain.models.Campagne;

@Component
public class CampagneMapper {

    // 1. Transforme la requête du client en Entité pour la base de données
    public Campagne toEntity(CampagneRequest request) {
        if (request == null) {
            return null;
        }

        Campagne campagne = new Campagne();
        campagne.setNom(request.nom());
        campagne.setSujet(request.sujet());
        campagne.setContenu(request.contenu());
        campagne.setDatePlanification(request.datePlanification());

        // Le statut par défaut (BROUILLON) est déjà géré par l'entité, mais on peut le forcer au besoin
        campagne.setStatut(CampagneStatus.BROUILLON);

        return campagne;
    }

    // 2. Transforme l'Entité de la base de données en DTO pour le client
    public CampagneResponse toResponse(Campagne campagne) {
        if (campagne == null) {
            return null;
        }

        return new CampagneResponse(
                campagne.getId(),
                campagne.getTrackingId(),
                campagne.getNom(),
                campagne.getSujet(),
                campagne.getContenu(),
                campagne.getStatut(),
                campagne.getDatePlanification(),
                campagne.getCreatedAt(),
                campagne.getUpdatedAt()
        );
    }

    // 3. Méthode utilitaire pour la mise à jour (le 'U' du CRUD)
    public void updateEntityFromRequest(Campagne campagne, CampagneRequest request) {
        if (request == null || campagne == null) {
            return;
        }

        campagne.setNom(request.nom());
        campagne.setSujet(request.sujet());
        campagne.setContenu(request.contenu());
        campagne.setDatePlanification(request.datePlanification());
    }
}

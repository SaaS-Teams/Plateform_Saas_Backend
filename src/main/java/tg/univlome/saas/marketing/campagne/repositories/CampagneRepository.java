package tg.univlome.saas.marketing.campagne.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.marketing.campagne.domain.enums.CampagneStatus;
import tg.univlome.saas.marketing.campagne.domain.models.Campagne;

@Repository
public interface CampagneRepository extends JpaRepository<Campagne, Long> {

// Spring Boot va comprendre automatiquement cette signature et générer le SQL !
// Cela nous sera très utile plus tard pour le tableau de bord analytique.
    long countByStatut(CampagneStatus statut);
    Optional<Campagne> findByTrackingId(UUID trackingId);
}

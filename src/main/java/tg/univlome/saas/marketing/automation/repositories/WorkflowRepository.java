package tg.univlome.saas.marketing.automation.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.marketing.automation.domain.models.Workflow;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    Optional<Workflow> findByTrackingId(UUID trackingId);
}

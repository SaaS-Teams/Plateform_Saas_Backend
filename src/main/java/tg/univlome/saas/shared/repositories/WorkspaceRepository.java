package tg.univlome.saas.shared.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.shared.domain.models.Workspace;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findByTrackingId(UUID trackingId);

    Optional<Workspace> findByName(String name);
}

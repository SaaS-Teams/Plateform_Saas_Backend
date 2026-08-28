package tg.univlome.saas.shared.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.shared.domain.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOnboardingUuid(UUID onboardingUuid);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}

package tg.univlome.saas.marketing.reseauxsociaux.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.univlome.saas.marketing.reseauxsociaux.domain.models.SocialAccount;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    List<SocialAccount> findByUserId(Long userId);

    Optional<SocialAccount> findByTrackingId(UUID trackingId);

    Optional<SocialAccount> findByUserIdAndPlatformKey(Long userId, String platformKey);
}

package tg.univlome.saas.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configuration de l'audit JPA pour hydrater automatiquement 
 * les champs createdBy, createdAt, updatedBy, updatedAt des entités (BaseEntity).
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Fournit le nom de l'utilisateur actuellement connecté à JPA.
     *
     * @return Le nom d'utilisateur ou "system" si aucun utilisateur n'est connecté.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }

            // Retourne le nom d'utilisateur (qui sera extrait du JWT plus tard)
            return Optional.of(authentication.getName());
        };
    }
}

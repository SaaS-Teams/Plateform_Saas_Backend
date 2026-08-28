package tg.univlome.saas.shared.security.tenant;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Aspect AOP gérant l'activation/désactivation automatique du filtre de cloisonnement Hibernate tenantFilter.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TenantFilterAspect {

    private static final String TENANT_FILTER_NAME = "tenantFilter";
    private static final String TENANT_PARAMETER_NAME = "tenantId";

    private final EntityManager entityManager;

    /**
     * Intercepte toutes les invocations de méthodes sur les repositories Spring Data.
     *
     * @param joinPoint le point de jonction AOP
     * @return le résultat de l'exécution du repository
     * @throws Throwable en cas d'erreur lors de l'exécution de la méthode d'origine
     */
    @Around("execution(* tg.univlome.saas..repositories..*(..))")
    public Object applyTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID tenantId = TenantContextHolder.getTenantId();
        Session session = entityManager.unwrap(Session.class);

        if (tenantId != null) {
            log.trace("[TENANT ASPECT] Activation du filtre Hibernate [{}] avec tenantId [{}]",
                    TENANT_FILTER_NAME, tenantId);
            session.enableFilter(TENANT_FILTER_NAME)
                    .setParameter(TENANT_PARAMETER_NAME, tenantId);
        } else {
            log.trace("[TENANT ASPECT] Contexte tenant null — Désactivation du filtre Hibernate [{}]",
                    TENANT_FILTER_NAME);
            session.disableFilter(TENANT_FILTER_NAME);
        }

        return joinPoint.proceed();
    }
}

package tg.univlome.saas.shared.security.tenant;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Composant utilitaire thread-safe gérant le contexte du Tenant (Workspace) courant pour la requête HTTP.
 *
 * <p>Utilise un {@link ThreadLocal} pour conserver l'UUID du workspace client pendant le cycle de vie de la requête.</p>
 */
@Slf4j
public final class TenantContextHolder {

    private static final ThreadLocal<UUID> TENANT_CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {
        // Classe utilitaire privée
    }

    /**
     * Définit l'identifiant (UUID) du Tenant pour le thread courant.
     *
     * @param tenantId l'UUID de l'espace client (workspace)
     */
    public static void setTenantId(UUID tenantId) {
        log.debug("[TENANT CONTEXT] Positionnement du tenantId: [{}]", tenantId);
        TENANT_CONTEXT.set(tenantId);
    }

    /**
     * Récupère l'identifiant du Tenant courant.
     *
     * @return l'UUID du Tenant, ou null s'il n'est pas défini
     */
    public static UUID getTenantId() {
        return TENANT_CONTEXT.get();
    }

    /**
     * Nettoie le ThreadLocal pour éviter toute fuite de mémoire entre requêtes dans le pool Tomcat.
     */
    public static void clear() {
        log.debug("[TENANT CONTEXT] Nettoyage du tenantId pour le thread courant.");
        TENANT_CONTEXT.remove();
    }
}

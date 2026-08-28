package tg.univlome.saas.shared.security.tenant;

import jakarta.persistence.PrePersist;
import java.lang.reflect.Field;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA EntityListener pour l'auto-assignation automatique du Tenant ID (workspaceTrackingId) lors d'un persist.
 */
@Slf4j
public class TenantListener {

    @PrePersist
    public void setTenantId(Object entity) {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return;
        }

        try {
            Field field = findField(entity.getClass(), "workspaceTrackingId");
            if (field != null) {
                field.setAccessible(true);
                Object currentValue = field.get(entity);
                if (currentValue == null) {
                    field.set(entity, tenantId);
                    log.debug("[TENANT LISTENER] Assignation automatique du workspaceTrackingId [{}] sur l'entité {}",
                            tenantId, entity.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            log.warn("[TENANT LISTENER] Impossible d'affecter le workspaceTrackingId sur l'entité {} : {}",
                    entity.getClass().getSimpleName(), e.getMessage());
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}

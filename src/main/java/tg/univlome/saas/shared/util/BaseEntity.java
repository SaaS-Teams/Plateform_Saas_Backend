package tg.univlome.saas.shared.util;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tg.univlome.saas.shared.security.tenant.TenantContextHolder;

/**
 * Classe de base pour toutes les entités avec audit et isolation Multi-Tenancy automatiques.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = java.util.UUID.class)
)
@Filter(
        name = "tenantFilter",
        condition = "workspace_tracking_id = :tenantId"
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_STRING_LENGTH = 100;

    @Column(name = "workspace_tracking_id")
    private UUID workspaceTrackingId;

    @Column(updatable = false, length = MAX_STRING_LENGTH)
    @CreatedBy
    private String createdBy;

    @Column(length = MAX_STRING_LENGTH)
    @LastModifiedBy
    private String updatedBy;

    @Column(updatable = false)
    @CreatedDate
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @LastModifiedDate
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onBaseEntityPrePersist() {
        if (this.workspaceTrackingId == null) {
            UUID currentTenantId = TenantContextHolder.getTenantId();
            if (currentTenantId != null) {
                this.workspaceTrackingId = currentTenantId;
            }
        }
    }
}

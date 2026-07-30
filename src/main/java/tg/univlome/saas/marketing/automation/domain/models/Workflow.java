package tg.univlome.saas.marketing.automation.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.ParamDef;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tg.univlome.saas.marketing.automation.domain.enums.WorkflowStatus;
import tg.univlome.saas.shared.security.tenant.TenantListener;

@Entity
@Table(name = "workflows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners({AuditingEntityListener.class, TenantListener.class})
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = java.util.UUID.class)
)
@Filter(
        name = "tenantFilter",
        condition = "workspace_tracking_id = :tenantId"
)
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // L'identifiant unique de traçabilité pour RabbitMQ et les logs externes
    @Column(name = "tracking_id", unique = true, updatable = false, nullable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "workspace_tracking_id")
    private UUID workspaceTrackingId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStatus status;

    // Type de déclencheur (ex: "COMPORTEMENT_CLIC", "DATE_ANNIVERSAIRE")
    @Column(name = "trigger_type")
    private String triggerType;

    // C'est ici la magie : on stocke l'arbre logique généré par le front-end directement en JSONB
    @JdbcTypeCode(SqlTypes.JSON)

    @Column(name = "flow_data", columnDefinition = "jsonb", nullable = false)
    private String flowData;

    @Column(name = "canvas_definition", columnDefinition = "TEXT")
    private String canvasDefinition;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

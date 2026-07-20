package tg.univlome.saas.marketing.automation.domain.models;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tg.univlome.saas.marketing.automation.domain.enums.WorkflowStatus;

@Entity
@Table(name = "workflows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // L'identifiant unique de traçabilité pour RabbitMQ et les logs externes
    @Column(name = "tracking_id", unique = true, updatable = false, nullable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

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

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

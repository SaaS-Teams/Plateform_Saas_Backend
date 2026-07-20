package tg.univlome.saas.marketing.automation.domain.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tg.univlome.saas.marketing.automation.domain.enums.ExecutionStatus;
import tg.univlome.saas.marketing.contact.domain.models.Contact; // Importation de ta vraie entité Contact

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WorkflowExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "execution_tracking_id", unique = true, updatable = false, nullable = false)
    @Builder.Default
    private UUID executionTrackingId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    // LA CORRECTION EST ICI : Vraie relation JPA avec l'entité Contact existante
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    @Column(name = "current_node_id")
    private String currentNodeId;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @CreatedDate
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
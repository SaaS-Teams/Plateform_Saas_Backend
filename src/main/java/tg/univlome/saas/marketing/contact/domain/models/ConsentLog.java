package tg.univlome.saas.marketing.contact.domain.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentAction;

@Entity
@Table(name = "CONSENT_LOGS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", unique = true, nullable = false, updatable = false)
    private UUID trackingId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", referencedColumnName = "id", nullable = false)
    private Contact contact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsentAction action;

    @Column(name = "ip_address")
    private String ipAddress;

    public ConsentLog(Contact contact, ConsentAction action, String ipAddress) {
        this.contact = contact;
        this.action = action;
        this.ipAddress = ipAddress;
    }

    @PrePersist
    protected void onCreate() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }
}


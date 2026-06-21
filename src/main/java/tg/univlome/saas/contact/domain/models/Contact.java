package tg.univlome.saas.marketing.contact.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tg.univlome.saas.marketing.contact.domain.enums.ConsentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CONTACTS")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contact {

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

    // --- CHAMPS MÉTIER ---
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_status", nullable = false)
    private ConsentStatus consentStatus  = ConsentStatus.PENDING;

    @PrePersist
    protected void onCreate() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }


}
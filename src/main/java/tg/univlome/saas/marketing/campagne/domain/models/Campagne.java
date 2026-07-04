package tg.univlome.saas.marketing.campagne.domain.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tg.univlome.saas.marketing.campagne.domain.enums.CampagneStatus;

@Entity
@Table(name = "campagnes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campagne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID trackingId = UUID.randomUUID();

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String sujet;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampagneStatus statut = CampagneStatus.BROUILLON;

    @Column(nullable = true)
    private LocalDateTime datePlanification;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Met à jour automatiquement la date de modification avant chaque UPDATE en base
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

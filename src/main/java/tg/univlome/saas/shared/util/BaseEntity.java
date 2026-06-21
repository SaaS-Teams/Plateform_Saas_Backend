package tg.univlome.saas.shared.util;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Classe de base pour toutes les entités avec audit automatique.
 * Toutes les entités du projet doivent hériter de cette classe.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_STRING_LENGTH = 100;

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
}

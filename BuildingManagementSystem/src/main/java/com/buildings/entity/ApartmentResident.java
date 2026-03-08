package com.buildings.entity;


import com.buildings.entity.Apartment;
import com.buildings.entity.enums.ResidentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "apartment_residents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApartmentResident extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_apartment_resident_apartment"))
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name =  "fk_apartment_resident_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "resident_type", nullable = false, length = 20)
    private ResidentType residentType;

    @Column(name = "id_card_number", length = 50)
    private String idCardNumber;

    @Column(name = "contract_details", columnDefinition = "TEXT")
    private String contractDetails;

    @Column(name = "ownership_certificate", length = 255)
    private String ownershipCertificate;

    @Column(name = "legal_docs", columnDefinition = "TEXT")
    private String legalDocs;

    @Column(length = 1000)
    private String note;

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "moved_out_at")
    private LocalDateTime movedOutAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Transient
    public boolean isCurrent() {
        return movedOutAt == null;
    }


    @Transient
    public boolean isOwner() {
        return residentType == ResidentType.OWNER;
    }


    @Transient
    public boolean hasCompleteLegalDocs() {
        if (residentType != ResidentType.OWNER) {
            return true;
        }
        return contractDetails != null && !contractDetails.trim().isEmpty() &&
                ownershipCertificate != null && !ownershipCertificate.trim().isEmpty();
    }
}


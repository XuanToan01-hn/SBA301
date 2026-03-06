package com.buildings.entity;

import com.buildings.entity.enums.ApartmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "apartments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"building_id", "code"}, name = "uk_apartment_code_building")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Apartment extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false, foreignKey = @ForeignKey(name = "fk_apartment_building"))
    private Building building;


    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "floor_number", nullable = false)
    private int floorNumber;

    @Column(name = "area_sqm", nullable = false)
    private double areaSqm;

    @Column(name = "bedroom_count", nullable = false)
    private int bedroomCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApartmentStatus status = ApartmentStatus.AVAILABLE;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApartmentResident> residents;
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MonthlyBills> monthlyBills;
    @Transient
    public Long getCurrentResidentsCount() {
        if (residents == null) return 0L;
        return residents.stream()
                .filter(r -> r.getMovedOutAt() == null)
                .count();
    }

}


package com.buildings.entity;

import com.buildings.entity.enums.ReviewOutcome;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "maintenance_reviews")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MaintenanceReview extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "maintenance_request_id", nullable = false)
    private MaintenanceRequest maintenanceRequest;

    private Integer rating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    private ReviewOutcome outcome;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;
}

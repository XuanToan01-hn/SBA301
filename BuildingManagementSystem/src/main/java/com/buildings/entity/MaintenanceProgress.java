package com.buildings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "maintenance_progresses")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MaintenanceProgress extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "maintenance_request_id", nullable = false)
    private MaintenanceRequest maintenanceRequest;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String note;

    private Integer progressPercent; // 0-100

    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;
}

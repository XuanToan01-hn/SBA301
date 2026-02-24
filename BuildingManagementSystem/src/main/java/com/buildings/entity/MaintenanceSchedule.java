package com.buildings.entity;

import com.buildings.entity.enums.ScheduleProposedBy;
import com.buildings.entity.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_schedules")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MaintenanceSchedule extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "maintenance_request_id", nullable = false)
    private MaintenanceRequest maintenanceRequest;

    @Column(nullable = false)
    private LocalDateTime proposedTime;

    private Integer estimatedDuration; // phút

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;

    @Enumerated(EnumType.STRING)
    private ScheduleProposedBy proposedByRole;

    @ManyToOne
    @JoinColumn(name = "proposed_by_id")
    private User proposedBy;

    // Nếu là COUNTER_PROPOSE, trỏ về schedule gốc
    @ManyToOne
    @JoinColumn(name = "parent_schedule_id")
    private MaintenanceSchedule parentSchedule;
}

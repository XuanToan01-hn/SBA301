package com.buildings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "maintenance_logs")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MaintenanceLog extends BaseEntity {

    @Column(columnDefinition = "VARCHAR(36)")
    private String requestId;

    @Column(columnDefinition = "VARCHAR(36)")
    private String actorId;

    private String action;

    private String note;    
}

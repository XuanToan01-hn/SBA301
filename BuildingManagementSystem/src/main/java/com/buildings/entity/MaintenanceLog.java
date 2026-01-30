package com.buildings.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "maintenance_logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MaintenanceLog extends BaseEntity {

    private UUID reuqestId;

    private UUID actorId;

    private String action;

    private String note;    
}

package com.buildings.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID requestId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID actorId;

    private String action;

    private String note;    
}

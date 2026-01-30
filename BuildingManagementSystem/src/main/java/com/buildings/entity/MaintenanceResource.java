package com.buildings.entity;

import com.buildings.entity.enums.ResourceType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "maintenance_resources")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MaintenanceResource extends BaseEntity {

    private String name;

    private String url;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @ManyToOne
    @JoinColumn(name = "maintenance_request_id")
    private MaintenanceRequest maintenanceRequest;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private MaintenanceItem item;
}

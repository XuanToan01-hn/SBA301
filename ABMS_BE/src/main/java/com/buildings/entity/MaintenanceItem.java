package com.buildings.entity;

import java.math.BigDecimal;

import com.buildings.entity.enums.ItemType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "maintenance_items")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MaintenanceItem extends BaseEntity {

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    private Integer quantity;

    @Column(precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @ManyToOne
    @JoinColumn(name = "quotation_id")
    private MaintenanceQuotation quotation;

}

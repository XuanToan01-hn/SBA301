package com.buildings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "buildings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public class Building extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String address;

    @Column(name = "num_floors")
    private int numFloors;

    @Column(name = "apartments_per_floor_1br")
    private int apartmentsPerFloor1Br;

    @Column(name = "apartments_per_floor_2br")
    private int apartmentsPerFloor2Br;

    @Column(name = "apartments_per_floor_3br")
    private int apartmentsPerFloor3Br;

    @Column(name = "area_1br_sqm")
    private double area1BrSqm;

    @Column(name = "area_2br_sqm")
    private double area2BrSqm;

    @Column(name = "area_3br_sqm")
    private double area3BrSqm;

    @Column(name = "apartments_generated")
    private boolean apartmentsGenerated;
}


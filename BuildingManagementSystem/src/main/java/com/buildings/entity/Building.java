package com.buildings.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "buildings")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Building extends BaseEntity{

    private String name;
}

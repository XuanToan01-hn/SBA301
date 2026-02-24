package com.buildings.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    // Relationships
    @OneToMany(mappedBy = "role")
    private List<UserRole> userRoles;

    // Common role codes
    public static final String ROLE_BUILDING_MANAGER = "BUILDING_MANAGER";
    public static final String ROLE_RESIDENT = "RESIDENT";
    public static final String ROLE_STAFF = "STAFF";
}


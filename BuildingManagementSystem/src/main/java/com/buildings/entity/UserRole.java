package com.buildings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing the relationship between users and roles
 * A user can have multiple roles, and a role can be building-specific or system-wide
 */
@Entity
@Table(name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "role_id", "building_id"},
                        name = "uk_user_role_building")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserRole extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role_role"))
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", foreignKey = @ForeignKey(name = "fk_user_role_building"))
    private Building building;

    /**
     * Check if this is a system-wide role (not building-specific)
     */
    @Transient
    public boolean isSystemWide() {
        return building == null;
    }

    /**
     * Check if this is a building-specific role
     */
    @Transient
    public boolean isBuildingSpecific() {
        return building != null;
    }
}


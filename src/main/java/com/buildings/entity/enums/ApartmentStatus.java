package com.buildings.entity.enums;

public enum ApartmentStatus {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    MAINTENANCE("Under Maintenance"),
    RESERVED("Reserved");

    private final String displayName;

    ApartmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

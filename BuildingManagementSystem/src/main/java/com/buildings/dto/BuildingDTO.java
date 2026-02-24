package com.buildings.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingDTO {

    private Long id;

    @NotBlank(message = "Building name is required")
    @Size(max = 100, message = "Building name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Building code is required")
    @Size(max = 50, message = "Building code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Building code must contain only uppercase letters, numbers, hyphens, and underscores")
    private String code;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @NotNull(message = "Number of floors is required")
    @Min(value = 1, message = "Number of floors must be at least 1")
    @Max(value = 100, message = "Number of floors must not exceed 100")
    private int numFloors;

    @NotNull(message = "Number of 1BR apartments per floor is required")
    @Min(value = 0, message = "Number of 1BR apartments cannot be negative")
    private int apartmentsPerFloor1br;

    @NotNull(message = "Number of 2BR apartments per floor is required")
    @Min(value = 0, message = "Number of 2BR apartments cannot be negative")
    private int apartmentsPerFloor2br;

    @NotNull(message = "Number of 3BR apartments per floor is required")
    @Min(value = 0, message = "Number of 3BR apartments cannot be negative")
    private int apartmentsPerFloor3br;

    @DecimalMin(value = "0.01", message = "Area for 1BR apartments must be greater than 0")
    private double area1brSqm;

    @DecimalMin(value = "0.01", message = "Area for 2BR apartments must be greater than 0")
    private double area2brSqm;

    @DecimalMin(value = "0.01", message = "Area for 3BR apartments must be greater than 0")
    private double area3brSqm;

    private Boolean apartmentsGenerated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Calculated fields
    private Integer totalApartmentsPerFloor;
    private Integer totalCapacity;
    private Long currentApartmentCount;

    /**
     * Custom validation to ensure at least one apartment type is defined
     */
    @AssertTrue(message = "At least one apartment type must be defined per floor")
    public boolean isValidApartmentLayout() {
        if (apartmentsPerFloor1br == null || apartmentsPerFloor2br == null || apartmentsPerFloor3br == null) {
            return true; // Let @NotNull handle this
        }
        return (apartmentsPerFloor1br + apartmentsPerFloor2br + apartmentsPerFloor3br) > 0;
    }

    /**
     * Custom validation for areas based on apartment counts
     */
    @AssertTrue(message = "Area must be provided for apartment types with count > 0")
    public boolean isValidAreaConfiguration() {
        if (apartmentsPerFloor1br != null && apartmentsPerFloor1br > 0 &&
                (area1brSqm == null || area1brSqm.compareTo(BigDecimal.ZERO) <= 0)) {
            return false;
        }
        if (apartmentsPerFloor2br != null && apartmentsPerFloor2br > 0 &&
                (area2brSqm == null || area2brSqm.compareTo(BigDecimal.ZERO) <= 0)) {
            return false;
        }
        if (apartmentsPerFloor3br != null && apartmentsPerFloor3br > 0 &&
                (area3brSqm == null || area3brSqm.compareTo(BigDecimal.ZERO) <= 0)) {
            return false;
        }
        return true;
    }
}


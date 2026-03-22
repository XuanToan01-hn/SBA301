package com.buildings.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingDTO {

    private UUID id;

    // ================= BASIC INFO =================

    @NotBlank(message = "Building name is required")
    @Size(max = 50, message = "Building name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Building code is required")
    @Size(max = 20, message = "Building code must not exceed 20 characters")
    private String code;

    @NotBlank(message = "Address is required")
    @Size(max = 50, message = "Address must not exceed 50 characters")
    private String address;

    // ================= FLOOR =================

    @NotNull(message = "Number of floors is required")
    @Min(value = 1, message = "Number of floors must be at least 1")
    @Max(value = 99, message = "Number of floors must not exceed 99")
    private Integer numFloors;

    // ================= 1BR =================

    @NotNull(message = "Number of 1BR apartments is required")
    @Min(value = 0, message = "1BR apartments cannot be negative")
    @Max(value = 20, message = "1BR apartments per floor must not exceed 20")
    private Integer apartmentsPerFloor1br;

    @NotNull(message = "Area of 1BR apartments is required")
    @DecimalMin(value = "20.0", message = "1BR area must be at least 20 sqm")
    @DecimalMax(value = "80.0", message = "1BR area must not exceed 80 sqm")
    private Double area1brSqm;

    // ================= 2BR =================

    @NotNull(message = "Number of 2BR apartments is required")
    @Min(value = 0, message = "2BR apartments cannot be negative")
    @Max(value = 20, message = "2BR apartments per floor must not exceed 20")
    private Integer apartmentsPerFloor2br;

    @NotNull(message = "Area of 2BR apartments is required")
    @DecimalMin(value = "40.0", message = "2BR area must be at least 40 sqm")
    @DecimalMax(value = "150.0", message = "2BR area must not exceed 150 sqm")
    private Double area2brSqm;

    // ================= 3BR =================

    @NotNull(message = "Number of 3BR apartments is required")
    @Min(value = 0, message = "3BR apartments cannot be negative")
    @Max(value = 20, message = "3BR apartments per floor must not exceed 20")
    private Integer apartmentsPerFloor3br;

    @NotNull(message = "Area of 3BR apartments is required")
    @DecimalMin(value = "60.0", message = "3BR area must be at least 60 sqm")
    @DecimalMax(value = "300.0", message = "3BR area must not exceed 300 sqm")
    private Double area3brSqm;

    // ================= SYSTEM =================

    private Boolean apartmentsGenerated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer totalApartmentsPerFloor;
    private Integer totalCapacity;
    private Long currentApartmentCount;

    // ================= BUSINESS VALIDATION =================

    @AssertTrue(message = "At least one apartment type must be greater than 0")
    public boolean isValidApartmentLayout() {
        if (apartmentsPerFloor1br == null ||
                apartmentsPerFloor2br == null ||
                apartmentsPerFloor3br == null) {
            return true;
        }

        return (apartmentsPerFloor1br
                + apartmentsPerFloor2br
                + apartmentsPerFloor3br) > 0;
    }

    @AssertTrue(message = "Area must be provided for apartment types with count > 0")
    public boolean isValidAreaConfiguration() {

        if (apartmentsPerFloor1br != null && apartmentsPerFloor1br > 0) {
            if (area1brSqm == null || area1brSqm <= 0) return false;
        }

        if (apartmentsPerFloor2br != null && apartmentsPerFloor2br > 0) {
            if (area2brSqm == null || area2brSqm <= 0) return false;
        }

        if (apartmentsPerFloor3br != null && apartmentsPerFloor3br > 0) {
            if (area3brSqm == null || area3brSqm <= 0) return false;
        }

        return true;
    }

    @AssertTrue(message = "Total apartments per floor must not exceed 30")
    public boolean isValidTotalApartments() {
        if (apartmentsPerFloor1br == null ||
                apartmentsPerFloor2br == null ||
                apartmentsPerFloor3br == null) {
            return true;
        }

        return (apartmentsPerFloor1br
                + apartmentsPerFloor2br
                + apartmentsPerFloor3br) <= 30;
    }
}
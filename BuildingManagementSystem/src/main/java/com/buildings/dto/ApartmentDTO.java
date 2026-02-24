package com.buildings.dto;


import com.buildings.entity.enums.ApartmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentDTO {

    private UUID id;

    @NotNull(message = "Building ID is required")
    private UUID buildingId;

    private String buildingName;
    private String buildingCode;

    @NotBlank(message = "Apartment code is required")
    @Size(max = 50, message = "Apartment code must not exceed 50 characters")
    private String code;

    @NotNull(message = "Floor number is required")
    @Min(value = 1, message = "Floor number must be at least 1")
    private int floorNumber;

    @NotNull(message = "Area is required")
    @DecimalMin(value = "0.01", message = "Area must be greater than 0")
    private double areaSqm;

    @NotNull(message = "Bedroom count is required")
    @Min(value = 1, message = "Bedroom count must be at least 1")
    @Max(value = 3, message = "Bedroom count must not exceed 3")
    private int bedroomCount;

    @NotNull(message = "Status is required")
    private ApartmentStatus status;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private int currentResidentsCount;
    private Boolean hasOwner;
    private String ownerName;
    private String ownerEmail;
}


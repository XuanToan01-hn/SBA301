package com.buildings.dto;


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
public class BuildingDTO {

    private UUID id; // Đảm bảo Entity Building cũng phải là UUID, nếu không hãy đổi cả 2 thành Long

    @NotBlank(message = "Building name is required")
    private String name;

    @NotBlank(message = "Building code is required")
    private String code;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Number of floors is required")
    @Min(1) @Max(100)
    private Integer numFloors; // Dùng Integer để @NotNull có tác dụng

    @NotNull(message = "Number of 1BR apartments is required")
    @Min(0)
    private Integer apartmentsPerFloor1br;

    @NotNull(message = "Number of 2BR apartments is required")
    @Min(0)
    private Integer apartmentsPerFloor2br;

    @NotNull(message = "Number of 3BR apartments is required")
    @Min(0)
    private Integer apartmentsPerFloor3br;

    // Chuyển sang Double hoặc BigDecimal để dùng validation chính xác
    @DecimalMin(value = "0.01", message = "Area must be greater than 0")
    private Double area1brSqm;

    @DecimalMin(value = "0.01")
    private Double area2brSqm;

    @DecimalMin(value = "0.01")
    private Double area3brSqm;

    private Boolean apartmentsGenerated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer totalApartmentsPerFloor;
    private Integer totalCapacity;
    private Long currentApartmentCount;

    @AssertTrue(message = "At least one apartment type must be defined per floor")
    public boolean isValidApartmentLayout() {
        if (apartmentsPerFloor1br == null || apartmentsPerFloor2br == null || apartmentsPerFloor3br == null) {
            return true;
        }
        return (apartmentsPerFloor1br + apartmentsPerFloor2br + apartmentsPerFloor3br) > 0;
    }

    @AssertTrue(message = "Area must be provided for apartment types with count > 0")
    public boolean isValidAreaConfiguration() {
        // Sửa lỗi so sánh double ở đây
        if (apartmentsPerFloor1br != null && apartmentsPerFloor1br > 0 && (area1brSqm == null || area1brSqm <= 0)) {
            return false;
        }
        if (apartmentsPerFloor2br != null && apartmentsPerFloor2br > 0 && (area2brSqm == null || area2brSqm <= 0)) {
            return false;
        }
        if (apartmentsPerFloor3br != null && apartmentsPerFloor3br > 0 && (area3brSqm == null || area3brSqm <= 0)) {
            return false;
        }
        return true;
    }
}

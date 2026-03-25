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

    @NotBlank(message = "Tên tòa nhà là bắt buộc")
    @Size(max = 50, message = "Tên tòa nhà không được vượt quá 50 ký tự")
    private String name;

    @NotBlank(message = "Mã tòa nhà là bắt buộc")
    @Size(max = 20, message = "Mã tòa nhà không được vượt quá 20 ký tự")
    private String code;

    @NotBlank(message = "Địa chỉ là bắt buộc")
    @Size(max = 50, message = "Địa chỉ không được vượt quá 50 ký tự")
    private String address;

    // ================= FLOOR =================

    @NotNull(message = "Số tầng là bắt buộc")
    @Min(value = 1, message = "Số tầng phải lớn hơn hoặc bằng 1")
    @Max(value = 99, message = "Số tầng không được vượt quá 99")
    private Integer numFloors;

    // ================= 1BR =================

    @NotNull(message = "Số căn hộ 1 phòng ngủ là bắt buộc")
    @Min(value = 0, message = "Số căn hộ 1 phòng ngủ không được âm")
    @Max(value = 20, message = "Số căn hộ 1 phòng ngủ mỗi tầng không được vượt quá 20")
    private Integer apartmentsPerFloor1br;

    @NotNull(message = "Diện tích căn hộ 1 phòng ngủ là bắt buộc")
    @DecimalMin(value = "20.0", message = "Diện tích căn hộ 1 phòng ngủ phải lớn hơn hoặc bằng 20 m2")
    @DecimalMax(value = "80.0", message = "Diện tích căn hộ 1 phòng ngủ không được vượt quá 80 m2")
    private Double area1brSqm;

    // ================= 2BR =================

    @NotNull(message = "Số căn hộ 2 phòng ngủ là bắt buộc")
    @Min(value = 0, message = "Số căn hộ 2 phòng ngủ không được âm")
    @Max(value = 20, message = "Số căn hộ 2 phòng ngủ mỗi tầng không được vượt quá 20")
    private Integer apartmentsPerFloor2br;

    @NotNull(message = "Diện tích căn hộ 2 phòng ngủ là bắt buộc")
    @DecimalMin(value = "40.0", message = "Diện tích căn hộ 2 phòng ngủ phải lớn hơn hoặc bằng 40 m2")
    @DecimalMax(value = "150.0", message = "Diện tích căn hộ 2 phòng ngủ không được vượt quá 150 m2")
    private Double area2brSqm;

    // ================= 3BR =================

    @NotNull(message = "Số căn hộ 3 phòng ngủ là bắt buộc")
    @Min(value = 0, message = "Số căn hộ 3 phòng ngủ không được âm")
    @Max(value = 20, message = "Số căn hộ 3 phòng ngủ mỗi tầng không được vượt quá 20")
    private Integer apartmentsPerFloor3br;

    @NotNull(message = "Diện tích căn hộ 3 phòng ngủ là bắt buộc")
    @DecimalMin(value = "60.0", message = "Diện tích căn hộ 3 phòng ngủ phải lớn hơn hoặc bằng 60 m2")
    @DecimalMax(value = "300.0", message = "Diện tích căn hộ 3 phòng ngủ không được vượt quá 300 m2")
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

    @AssertTrue(message = "Phải có ít nhất một loại căn hộ có số lượng lớn hơn 0")
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

    @AssertTrue(message = "Phải nhập diện tích cho các loại căn hộ có số lượng > 0")
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

    @AssertTrue(message = "Tổng số căn hộ mỗi tầng không được vượt quá 30")
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
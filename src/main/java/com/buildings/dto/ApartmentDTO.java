package com.buildings.dto;

import com.buildings.entity.enums.ApartmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentDTO {

    private UUID id;

    private UUID buildingId;
    @NotNull(message = "Tên tòa nhà không được để trống")
    private String buildingName;
    @NotNull(message = "Mã tòa nhà không được để trống")
    private String buildingCode;

    @NotBlank(message = "Mã căn hộ không được để trống")
    @Size(max = 50, message = "Mã căn hộ không được vượt quá 50 ký tự")
    private String code;

    @Min(value = 1, message = "Số tầng phải lớn hơn hoặc bằng 1")
    private int floorNumber;

    @DecimalMin(value = "0.01", message = "Diện tích phải lớn hơn 0")
    private double areaSqm;

    @Min(value = 1, message = "Số phòng ngủ phải lớn hơn hoặc bằng 1")
    @Max(value = 3, message = "Số phòng ngủ không được vượt quá 3")
    private int bedroomCount;

    @NotNull(message = "Trạng thái căn hộ không được để trống")
    private ApartmentStatus status;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Min(value = 0, message = "Số người đang ở không được nhỏ hơn 0")
    private int currentResidentsCount;

    private Boolean hasOwner;

    @Size(max = 100, message = "Tên chủ sở hữu không được vượt quá 100 ký tự")
    private String ownerName;

    @Email(message = "Email chủ sở hữu không hợp lệ")
    @Size(max = 100, message = "Email chủ sở hữu không được vượt quá 100 ký tự")
    private String ownerEmail;
}
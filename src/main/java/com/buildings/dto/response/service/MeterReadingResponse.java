package com.buildings.dto.response.service;

import com.buildings.entity.enums.MeterReadingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterReadingResponse {

    private UUID id;
    private String period;

    // Thông tin căn hộ
    private UUID apartmentId;
    private String apartmentCode;

    // Thông tin dịch vụ
    private UUID serviceId;
    private String serviceCode;
    private String serviceName;
    private String serviceUnit;

    // Chỉ số
    private BigDecimal oldIndex;
    private BigDecimal newIndex;
    private BigDecimal consumption;
    private Boolean isMeterReset;

    // Thông tin ảnh
    private String photoUrl;
    private LocalDateTime takenAt;

    // Người ghi
    private UUID takenById;
    private String takenByName;

    // Trạng thái
    private MeterReadingStatus status;
    private String note;

    //Cảnh báo tiêu thụ bất thường (tăng > 300% so với tháng trước)
    private Boolean isHighUsage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

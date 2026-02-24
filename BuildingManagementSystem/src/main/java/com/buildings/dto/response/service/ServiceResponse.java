package com.buildings.dto.response.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.buildings.entity.enums.BillingMethod;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private String unit;
    private Boolean isRecurring;
    private BillingMethod billingMethod;
    private Boolean taxable;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin biểu giá hiện tại (nếu cần)
    private ServiceTariffResponse currentTariff;

    // Danh sách tất cả biểu giá
    private List<ServiceTariffResponse> tariffs;
}

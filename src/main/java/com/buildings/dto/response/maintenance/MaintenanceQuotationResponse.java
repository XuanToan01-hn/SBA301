package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.QuotationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MaintenanceQuotationResponse {
    private UUID id;
    private String code;
    private String title;
    private QuotationStatus status;
    private String description;
    private String note;
    private BigDecimal totalAmount;
    private LocalDateTime validUntil;
    private List<MaintenanceItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

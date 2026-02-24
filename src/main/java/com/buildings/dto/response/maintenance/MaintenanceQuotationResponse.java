package com.buildings.dto.response.maintenance;

import com.buildings.entity.enums.QuotationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MaintenanceQuotationResponse {
    private String id;
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

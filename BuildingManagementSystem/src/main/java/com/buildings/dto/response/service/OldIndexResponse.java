package com.buildings.dto.response.service;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response cho API lấy chỉ số cũ suggest cho tháng mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OldIndexResponse {

    private UUID apartmentId;
    private UUID serviceId;
    private String serviceName;
    private String currentPeriod; // Kỳ hiện tại cần nhập
    private String previousPeriod; // Kỳ trước đó (nguồn của old_index)
    private BigDecimal suggestedOldIndex; // Chỉ số mới của kỳ trước = chỉ số cũ của kỳ này
    private Boolean hasPreviousReading; // True nếu có dữ liệu kỳ trước
}

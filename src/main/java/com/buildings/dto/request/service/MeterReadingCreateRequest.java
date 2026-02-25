package com.buildings.dto.request.service;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterReadingCreateRequest {

    @NotNull(message = "Apartment ID is required")
    private UUID apartmentId;

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    @NotBlank(message = "Period is required")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Period must be in format YYYY-MM")
    private String period;

    // old_index sẽ được tự động lấy từ tháng trước, nhưng cho phép override nếu cần
    private BigDecimal oldIndex;

    @NotNull(message = "New index is required")
    @DecimalMin(value = "0.0", message = "New index must be non-negative")
    private BigDecimal newIndex;

    @Builder.Default
    private Boolean isMeterReset = false; // True nếu thay đồng hồ hoặc quay vòng về 0

    private String note;
}

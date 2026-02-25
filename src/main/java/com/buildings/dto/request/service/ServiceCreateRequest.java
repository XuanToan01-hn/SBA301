package com.buildings.dto.request.service;

import com.buildings.entity.enums.BillingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCreateRequest {

    @NotBlank(message = "Service code is required")
    @Size(max = 50, message = "Service code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Service name is required")
    @Size(max = 100, message = "Service name must not exceed 100 characters")
    private String name;

    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;

    @Builder.Default
    private Boolean isRecurring = true;

    @NotNull(message = "Billing method is required")
    private BillingMethod billingMethod;

    @Builder.Default
    private Boolean taxable = true;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}

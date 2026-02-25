package com.buildings.dto.request.service;

import com.buildings.entity.enums.BillingMethod;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUpdateRequest {

    @Size(max = 100, message = "Service name must not exceed 100 characters")
    private String name;

    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;

    private Boolean isRecurring;

    private BillingMethod billingMethod;

    private Boolean taxable;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}

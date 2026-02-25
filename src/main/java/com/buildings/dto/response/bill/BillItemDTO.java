package com.buildings.dto.response.bill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BillItemDTO {
    private UUID id;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String type;
}

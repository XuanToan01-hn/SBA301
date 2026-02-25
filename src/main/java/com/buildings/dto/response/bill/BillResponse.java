package com.buildings.dto.response.bill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BillResponse {
    private UUID id;
    private String code;
    private BigDecimal totalAmount;
    private String status;
    private List<BillItemDTO> items;
}

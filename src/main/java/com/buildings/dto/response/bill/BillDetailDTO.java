package com.buildings.dto.response.bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailDTO {
    private UUID id;
    private String description;
    private Double quantity;
    private Double unitPrice;
    private Double amount;
    private Double taxRate;
    private Double totalLine;
}

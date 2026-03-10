package com.buildings.dto.response.bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {
    private UUID id;
    private UUID apartmentId;
    private String apartmentCode;
    private String periodCode;
    private LocalDateTime periodFrom;
    private LocalDateTime periodTo;
    private Double subtotal;
    private Double taxTotal;
    private Double totalAmount;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime dueDate;
    private Boolean locked;
    private List<BillDetailDTO> details;
}

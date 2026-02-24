package com.buildings.dto.request.bill;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BillNotification {

    private String billCode;
    private Long amount;
    private String message;
}
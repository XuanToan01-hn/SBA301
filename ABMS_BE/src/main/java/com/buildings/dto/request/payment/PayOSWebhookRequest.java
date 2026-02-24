package com.buildings.dto.request.payment;

import lombok.Data;

@Data
public class PayOSWebhookRequest {
    private String code;
    private String desc;
    private boolean success;
    private PayOSData data;
}



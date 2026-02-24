package com.buildings.dto.request.payment;


import lombok.Data;

@Data
public class PayOSData {
    private Long orderCode;
    private Long amount;
    private String description;
}
package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.response.bill.BillResponse;
import com.buildings.dto.response.payment.PaymentResponse;
import com.buildings.entity.Bill;
import com.buildings.service.impl.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bills")
public class BillController {

    private final BillService billService;

    // Lấy chi tiết bill
    @GetMapping("/{billId}")
    public ApiResponse<BillResponse> getBill(
            @PathVariable UUID billId) {
        System.out.println("Received ID: " + billId);

//        return ResponseEntity.ok(
//                billService.getBillDetail(billId)
//        );

        return ApiResponse.<BillResponse>builder()
                .result(billService.getBillDetail(billId))
                .build();
    }


    // Tạo payment link + trả QR
    @PostMapping("/{billId}/payment")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable UUID billId) throws Exception {

        return ResponseEntity.ok(
                billService.createPayment(billId)
        );
    }
}


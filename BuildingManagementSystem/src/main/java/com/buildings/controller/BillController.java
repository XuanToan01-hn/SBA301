package com.buildings.controller;

import com.buildings.dto.request.bill.BillNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public String createBill() {

        // 🔥 Hardcode demo
        String billCode = "HD-" + System.currentTimeMillis();
        Long amount = 1500000L;

        // TODO: save DB nếu có

        // 🔥 Gửi notification realtime
        BillNotification notification = BillNotification.builder()
                .billCode(billCode)
                .amount(amount)
                .message("Bạn có hóa đơn mới cần thanh toán!")
                .build();

        messagingTemplate.convertAndSend(
                "/topic/bills",
                notification
        );

        return "Bill created successfully";
    }
}


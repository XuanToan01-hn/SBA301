package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.PageResponse;
import com.buildings.dto.response.bill.BillDTO;
import com.buildings.entity.User;
import com.buildings.repository.UserRepository;
import com.buildings.service.MonthlyBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/monthly-bills")
@RequiredArgsConstructor
public class MonthlyBillController {

    private final MonthlyBillService monthlyBillService;
        private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BillDTO>>> getAllBills(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String unSort) {
        
        PageResponse<BillDTO> bills = monthlyBillService.getAllBills(page, size, sortBy, unSort);
        return ResponseEntity.ok(ApiResponse.<PageResponse<BillDTO>>builder()
                .result(bills)
                .message("Fetched all bills successfully")
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillDTO>> getBillDetails(@PathVariable UUID id) {
        BillDTO bill = monthlyBillService.getBillDetails(id);
        return ResponseEntity.ok(ApiResponse.<BillDTO>builder()
                .result(bill)
                .message("Fetched bill details successfully")
                .build());
    }

        @GetMapping("/maintenance/{requestId}/payable")
        public ResponseEntity<ApiResponse<BillDTO>> getPayableBillForMaintenance(
                        @PathVariable UUID requestId,
                        Authentication authentication) {
                Optional<User> user = userRepository.findByEmail(authentication.getName());
                BillDTO bill = monthlyBillService.getPayableMaintenanceBill(requestId, user.get().getId());
                return ResponseEntity.ok(ApiResponse.<BillDTO>builder()
                                .result(bill)
                                .message("Fetched payable maintenance bill successfully")
                                .build());
        }

    @GetMapping("/current-month")
    public ResponseEntity<ApiResponse<PageResponse<BillDTO>>> getBillsForCurrentMonth(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String unSort) {
        
        PageResponse<BillDTO> bills = monthlyBillService.getBillsForCurrentMonth(page, size, sortBy, unSort);
        return ResponseEntity.ok(ApiResponse.<PageResponse<BillDTO>>builder()
                .result(bills)
                .message("Fetched bills for current month successfully")
                .build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<BillDTO>>> getBillsByUser(
            @PathVariable UUID userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String periodCode,
            @RequestParam(required = false) String apartmentCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String unSort) {
        
        PageResponse<BillDTO> bills = monthlyBillService.getBillsByUser(userId, status, periodCode, apartmentCode, page, size, sortBy, unSort);
        return ResponseEntity.ok(ApiResponse.<PageResponse<BillDTO>>builder()
                .result(bills)
                .message("Fetched user bills successfully")
                .build());
    }
}

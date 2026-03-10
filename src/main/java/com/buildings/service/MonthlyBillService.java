package com.buildings.service;

import com.buildings.dto.PageResponse;
import com.buildings.dto.response.bill.BillDTO;

import java.time.YearMonth;
import java.util.UUID;

public interface MonthlyBillService {
    void generateMonthlyBills(YearMonth billingPeriod);

    PageResponse<BillDTO> getAllBills(int page, int size, String sortBy, String unSort);

    BillDTO getBillDetails(UUID id);

    PageResponse<BillDTO> getBillsForCurrentMonth(int page, int size, String sortBy, String unSort);

    PageResponse<BillDTO> getBillsByUser(UUID userId, String status, String periodCode, String apartmentCode, int page, int size, String sortBy, String unSort);
}

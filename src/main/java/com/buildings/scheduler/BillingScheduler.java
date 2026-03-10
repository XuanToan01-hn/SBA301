package com.buildings.scheduler;

import com.buildings.service.MonthlyBillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {

    private final MonthlyBillService monthlyBillService;

    /**
     * Chạy định kỳ vào lúc 00:00:00 ngày 10 hàng tháng
     * cron = "0 0 0 10 * ?"
     */
    @Scheduled(cron = "0 0 0 10 * ?")
    public void runMonthlyBillingJob() {
        log.info("triggering runMonthlyBillingJob...");
        
        // Tự động lùi về tháng trước
        // Ví dụ: Hôm nay là 10/04/2024 -> billing period là 03/2024
        LocalDate currentDate = LocalDate.now();
        YearMonth previousMonth = YearMonth.from(currentDate).minusMonths(1);
        
        try {
            monthlyBillService.generateMonthlyBills(previousMonth);
            log.info("Successfully finished runMonthlyBillingJob for {}", previousMonth);
        } catch (Exception e) {
            log.error("Failed to run runMonthlyBillingJob for {}: {}", previousMonth, e.getMessage(), e);
        }
    }
}

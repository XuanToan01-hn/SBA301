package com.buildings.repository;

import com.buildings.entity.PaymentTransaction;
import com.buildings.entity.enums.PaymentTransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    // Tránh crash khi có nhiều PENDING (race condition)
    Optional<PaymentTransaction> findFirstByBillIdAndStatusOrderByCreatedAtDesc(UUID billId, PaymentTransactionStatus status);

    List<PaymentTransaction> findAllByBillIdAndStatus(UUID billId, PaymentTransactionStatus status);

    Optional<PaymentTransaction> findByOrderCode(Long orderCode);

    // Lịch sử giao dịch với filter tùy chọn
    @Query("SELECT t FROM PaymentTransaction t " +
           "WHERE (:status IS NULL OR t.status = :status) " +
           "AND (:billId IS NULL OR t.bill.id = :billId)")
    Page<PaymentTransaction> findAllWithFilters(
            @Param("status") PaymentTransactionStatus status,
            @Param("billId") UUID billId,
            Pageable pageable);

    // Thống kê theo status
    long countByStatus(PaymentTransactionStatus status);

    // Tổng doanh thu từ các giao dịch thành công
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM PaymentTransaction t WHERE t.status = 'SUCCESS'")
    BigDecimal getTotalRevenue();

    // Doanh thu theo từng tháng (12 tháng gần nhất)
    @Query(value = "SELECT DATE_FORMAT(t.paid_at, '%Y-%m') AS month, " +
                   "SUM(t.amount) AS revenue, COUNT(*) AS cnt " +
                   "FROM payment_transactions t " +
                   "WHERE t.status = 'SUCCESS' AND t.paid_at IS NOT NULL " +
                   "GROUP BY month ORDER BY month DESC LIMIT 12",
           nativeQuery = true)
    List<Object[]> getMonthlyRevenueSummary();
}

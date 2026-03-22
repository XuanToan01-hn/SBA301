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
        Optional<PaymentTransaction> findFirstByBillIdAndStatusAndReferenceNoIsNullOrderByCreatedAtDesc(UUID billId, PaymentTransactionStatus status);

        Optional<PaymentTransaction> findFirstByBillIdAndStatusAndReferenceNoOrderByCreatedAtDesc(
                        UUID billId,
                        PaymentTransactionStatus status,
                        String referenceNo
        );

    List<PaymentTransaction> findAllByBillIdAndStatus(UUID billId, PaymentTransactionStatus status);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM PaymentTransaction t WHERE t.bill.id = :billId AND t.status = 'SUCCESS'")
        BigDecimal getPaidAmountByBillId(@Param("billId") UUID billId);

    Optional<PaymentTransaction> findByOrderCode(Long orderCode);

    // Lịch sử giao dịch với filter tùy chọn
    @Query("SELECT t FROM PaymentTransaction t " +
           "WHERE (:status IS NULL OR t.status = :status) " +
           "AND (:billId IS NULL OR t.bill.id = :billId) " +
           "AND (:month IS NULL OR MONTH(t.createdAt) = :month) " +
           "AND (:year IS NULL OR YEAR(t.createdAt) = :year)")
    Page<PaymentTransaction> findAllWithFilters(
            @Param("status") PaymentTransactionStatus status,
            @Param("billId") UUID billId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            Pageable pageable);

    // Thống kê theo status, filter theo tháng/năm (null = không filter)
    @Query("SELECT COUNT(t) FROM PaymentTransaction t WHERE t.status = :status " +
           "AND (:month IS NULL OR MONTH(t.createdAt) = :month) " +
           "AND (:year IS NULL OR YEAR(t.createdAt) = :year)")
    long countByStatusAndPeriod(
            @Param("status") PaymentTransactionStatus status,
            @Param("month") Integer month,
            @Param("year") Integer year);

    // Tổng doanh thu từ các giao dịch thành công, filter theo tháng/năm
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM PaymentTransaction t WHERE t.status = 'SUCCESS' " +
           "AND (:month IS NULL OR MONTH(t.paidAt) = :month) " +
           "AND (:year IS NULL OR YEAR(t.paidAt) = :year)")
    BigDecimal getTotalRevenue(@Param("month") Integer month, @Param("year") Integer year);

    // Doanh thu theo từng tháng (12 tháng gần nhất), filter theo năm
    @Query(value = "SELECT DATE_FORMAT(t.paid_at, '%Y-%m') AS month, " +
                   "SUM(t.amount) AS revenue, COUNT(*) AS cnt " +
                   "FROM payment_transactions t " +
                   "WHERE t.status = 'SUCCESS' AND t.paid_at IS NOT NULL " +
                   "AND (:year IS NULL OR YEAR(t.paid_at) = :year) " +
                   "GROUP BY month ORDER BY month DESC LIMIT 12",
           nativeQuery = true)
    List<Object[]> getMonthlyRevenueSummary(@Param("year") Integer year);

    // Danh sách PENDING có proof (chờ admin duyệt)
    @Query("SELECT t FROM PaymentTransaction t WHERE t.status = 'PENDING' AND t.proofUrl IS NOT NULL")
    Page<PaymentTransaction> findPendingWithProof(Pageable pageable);
}

package com.buildings.repository;

import com.buildings.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByOrderCode(Long orderCode);

    @Query("""
    SELECT COALESCE(SUM(p.amount),0)
    FROM PaymentTransaction p
    WHERE p.bill.id = :billId
    AND p.status = 'SUCCESS'
""")
    BigDecimal sumSuccessAmountByBill(UUID billId);

}

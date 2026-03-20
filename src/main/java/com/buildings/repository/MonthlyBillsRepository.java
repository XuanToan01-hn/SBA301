package com.buildings.repository;

import com.buildings.entity.MonthlyBills;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlyBillsRepository extends JpaRepository<MonthlyBills, UUID> {
    
    @Query("SELECT mb FROM MonthlyBills mb LEFT JOIN FETCH mb.details WHERE mb.id = :id")
    Optional<MonthlyBills> findByIdWithDetails(@Param("id") UUID id);

    Page<MonthlyBills> findByPeriodCode(String periodCode, Pageable pageable);

    Optional<MonthlyBills> findFirstByApartmentIdAndPeriodCode(UUID apartmentId, String periodCode);

    @Query("SELECT DISTINCT b FROM MonthlyBills b " +
           "JOIN b.details d " +
           "JOIN b.apartment a " +
           "JOIN a.residents r " +
           "WHERE r.user.id = :userId " +
           "AND LOWER(d.description) LIKE LOWER(CONCAT('%', :requestCode, '%')) " +
           "ORDER BY b.issuedAt DESC")
    Page<MonthlyBills> findByResidentAndMaintenanceRequestCode(
           @Param("userId") UUID userId,
           @Param("requestCode") String requestCode,
           Pageable pageable);

    @Query("SELECT DISTINCT b FROM MonthlyBills b JOIN b.apartment a JOIN a.residents r WHERE r.user.id = :userId " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:periodCode IS NULL OR b.periodCode = :periodCode) " +
           "AND (:apartmentCode IS NULL OR a.code = :apartmentCode) " +
           "AND (b.status IN ('UNPAID', 'PAID', 'PARTIAL'))")
    Page<MonthlyBills> findByUserId(
            @Param("userId") UUID userId, 
            @Param("status") String status, 
            @Param("periodCode") String periodCode, 
            @Param("apartmentCode") String apartmentCode, 
            Pageable pageable);
    @Query("SELECT DISTINCT b FROM MonthlyBills b JOIN b.apartment a JOIN a.building bg " +
           "WHERE (:status IS NULL OR b.status = :status) " +
           "AND (:periodCode IS NULL OR b.periodCode = :periodCode) " +
           "AND (:buildingCode IS NULL OR bg.code = :buildingCode)")
    Page<MonthlyBills> findAllWithFilters(
            @Param("status") String status,
            @Param("periodCode") String periodCode,
            @Param("buildingCode") String buildingCode,
            Pageable pageable);
}

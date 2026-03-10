package com.buildings.repository;

import com.buildings.entity.MaintenanceQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.UUID;

@Repository
public interface MaintenanceQuotationRepository extends JpaRepository<MaintenanceQuotation, UUID>, JpaSpecificationExecutor<MaintenanceQuotation> {
    List<MaintenanceQuotation> findByMaintenanceRequestId(UUID maintenanceRequestId);

    @org.springframework.data.jpa.repository.Query("SELECT q FROM MaintenanceQuotation q JOIN q.maintenanceRequest r " +
           "WHERE r.apartment.id = :apartmentId " +
           "AND q.status = 'APPROVED' " +
           "AND q.createdAt >= :periodStart AND q.createdAt <= :periodEnd")
    List<MaintenanceQuotation> findQuotationsForBilling(
            @org.springframework.data.repository.query.Param("apartmentId") UUID apartmentId, 
            @org.springframework.data.repository.query.Param("periodStart") java.time.LocalDateTime periodStart, 
            @org.springframework.data.repository.query.Param("periodEnd") java.time.LocalDateTime periodEnd);
}

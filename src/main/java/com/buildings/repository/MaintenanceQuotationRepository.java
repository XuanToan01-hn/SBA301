package com.buildings.repository;

import com.buildings.entity.MaintenanceQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceQuotationRepository extends JpaRepository<MaintenanceQuotation, String>, JpaSpecificationExecutor<MaintenanceQuotation> {
    List<MaintenanceQuotation> findByMaintenanceRequestId(String maintenanceRequestId);
}

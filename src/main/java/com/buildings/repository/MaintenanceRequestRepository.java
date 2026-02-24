package com.buildings.repository;

import com.buildings.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, UUID>, JpaSpecificationExecutor<MaintenanceRequest> {
    boolean existsByCode(String code);
}

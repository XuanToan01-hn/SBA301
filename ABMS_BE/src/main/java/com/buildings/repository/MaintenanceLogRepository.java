package com.buildings.repository;

import com.buildings.entity.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, UUID>, JpaSpecificationExecutor<MaintenanceLog> {
    List<MaintenanceLog> findByRequestId(UUID requestId);
}

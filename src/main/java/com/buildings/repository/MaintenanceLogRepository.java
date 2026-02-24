package com.buildings.repository;

import com.buildings.entity.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, String>, JpaSpecificationExecutor<MaintenanceLog> {
    List<MaintenanceLog> findByRequestId(String requestId);
}

package com.buildings.repository;

import com.buildings.entity.MaintenanceSchedule;
import com.buildings.entity.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, UUID> {
    List<MaintenanceSchedule> findByMaintenanceRequestIdOrderByCreatedAtAsc(UUID requestId);
    List<MaintenanceSchedule> findByMaintenanceRequestIdAndStatus(UUID requestId, ScheduleStatus status);
}

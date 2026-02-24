package com.buildings.repository;

import com.buildings.entity.MaintenanceSchedule;
import com.buildings.entity.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, String> {
    List<MaintenanceSchedule> findByMaintenanceRequestIdOrderByCreatedAtAsc(String requestId);
    List<MaintenanceSchedule> findByMaintenanceRequestIdAndStatus(String requestId, ScheduleStatus status);
}

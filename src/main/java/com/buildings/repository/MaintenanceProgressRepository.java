package com.buildings.repository;

import com.buildings.entity.MaintenanceProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceProgressRepository extends JpaRepository<MaintenanceProgress, String> {
    List<MaintenanceProgress> findByMaintenanceRequestIdOrderByCreatedAtAsc(String requestId);
}

package com.buildings.repository;

import com.buildings.entity.MaintenanceProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.UUID;

@Repository
public interface MaintenanceProgressRepository extends JpaRepository<MaintenanceProgress, UUID> {
    List<MaintenanceProgress> findByMaintenanceRequestIdOrderByCreatedAtAsc(UUID requestId);
}

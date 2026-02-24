package com.buildings.repository;

import com.buildings.entity.MaintenanceResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceResourceRepository extends JpaRepository<MaintenanceResource, UUID>, JpaSpecificationExecutor<MaintenanceResource> {
    List<MaintenanceResource> findByMaintenanceRequestId(UUID maintenanceRequestId);
}

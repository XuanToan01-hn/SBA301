package com.buildings.repository;

import com.buildings.entity.MaintenanceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaintenanceItemRepository extends JpaRepository<MaintenanceItem, UUID>, JpaSpecificationExecutor<MaintenanceItem> {
}

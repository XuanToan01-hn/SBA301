package com.buildings.repository;

import com.buildings.entity.MaintenanceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceItemRepository extends JpaRepository<MaintenanceItem, String>, JpaSpecificationExecutor<MaintenanceItem> {
}

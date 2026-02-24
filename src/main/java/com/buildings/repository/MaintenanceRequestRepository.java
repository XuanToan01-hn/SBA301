package com.buildings.repository;

import com.buildings.entity.MaintenanceRequest;
import com.buildings.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, String>, JpaSpecificationExecutor<MaintenanceRequest> {
    boolean existsByCode(String code);

    List<MaintenanceRequest> findByRequestStatusAndStartedAtBefore(RequestStatus status, LocalDateTime dateTime);

    @Query("SELECT r FROM MaintenanceRequest r WHERE r.staff IS NOT NULL")
    List<MaintenanceRequest> findAllWithStaff();
}

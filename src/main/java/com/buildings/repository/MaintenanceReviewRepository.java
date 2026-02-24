package com.buildings.repository;

import com.buildings.entity.MaintenanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaintenanceReviewRepository extends JpaRepository<MaintenanceReview, String> {
    Optional<MaintenanceReview> findByMaintenanceRequestId(String requestId);
    boolean existsByMaintenanceRequestId(String requestId);
}

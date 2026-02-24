package com.buildings.repository;

import com.buildings.entity.MaintenanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaintenanceReviewRepository extends JpaRepository<MaintenanceReview, UUID> {
    Optional<MaintenanceReview> findByMaintenanceRequestId(UUID requestId);
    boolean existsByMaintenanceRequestId(UUID requestId);
}

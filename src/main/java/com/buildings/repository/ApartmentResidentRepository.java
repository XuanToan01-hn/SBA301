package com.buildings.repository;

import com.buildings.entity.ApartmentResident;
import com.buildings.entity.enums.ResidentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface ApartmentResidentRepository extends JpaRepository<ApartmentResident, UUID> {
    Optional<ApartmentResident> findByUserIdAndApartmentIdAndMovedOutAtIsNull(UUID userId, UUID apartmentId);
    Optional<ApartmentResident> findFirstByUserIdAndMovedOutAtIsNullOrderByAssignedAtDesc(UUID userId);
    boolean existsByUserIdAndApartmentIdAndMovedOutAtIsNull(UUID userId, UUID apartmentId);
    @Query("SELECT r FROM ApartmentResident r WHERE r.apartment.id = :apartmentId " +
            "AND (:type IS NULL OR r.residentType = :type)")
    Page<ApartmentResident> findHistoryByApartmentId(
            @Param("apartmentId") UUID apartmentId,
            @Param("type") ResidentType type,
            Pageable pageable);

}
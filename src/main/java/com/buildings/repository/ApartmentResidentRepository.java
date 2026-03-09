package com.buildings.repository;

import com.buildings.entity.ApartmentResident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface ApartmentResidentRepository extends JpaRepository<ApartmentResident, UUID> {
    Optional<ApartmentResident> findByUserIdAndApartmentIdAndMovedOutAtIsNull(UUID userId, UUID apartmentId);

    boolean existsByUserIdAndApartmentIdAndMovedOutAtIsNull(UUID userId, UUID apartmentId);
}
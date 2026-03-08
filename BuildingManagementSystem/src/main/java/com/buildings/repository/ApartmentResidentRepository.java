package com.buildings.repository;

import com.buildings.entity.ApartmentResident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface ApartmentResidentRepository extends JpaRepository<ApartmentResident, UUID> {
    // Tìm cư dân đang ở (chưa dời đi) trong căn hộ cụ thể
    Optional<ApartmentResident> findByUserIdAndApartmentIdAndMovedOutAtIsNull(UUID userId, UUID apartmentId);

    // Kiểm tra xem user đã có trong căn hộ này chưa
    boolean existsByUserIdAndApartmentIdAndMovedOutAtIsNull(UUID userId, UUID apartmentId);
}
package com.buildings.repository;

import com.buildings.entity.Apartment;
import com.buildings.entity.ApartmentResident;
import com.buildings.entity.enums.ApartmentStatus;
import com.buildings.entity.enums.ResidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {

    @Query("SELECT a FROM Apartment a " +
            "LEFT JOIN FETCH a.building " +
            "LEFT JOIN FETCH a.residents r " +
            "LEFT JOIN FETCH r.user " +
            "WHERE a.id = :id")
    Optional<Apartment> findByIdFullInfo(@Param("id") UUID id);

    List<Apartment> findByBuildingIdOrderByFloorNumberAscCodeAsc(UUID buildingId);

    Page<Apartment> findByBuildingId(UUID buildingId, Pageable pageable);

    Long countByBuildingId(UUID buildingId);

    Long countByBuildingIdAndStatus(UUID buildingId, ApartmentStatus status);

    @Query("SELECT a FROM Apartment a WHERE a.building.id = :buildingId " +
            "AND (:code IS NULL OR a.code LIKE %:code%) " + // Thêm dòng này
            "AND (:floorNumber IS NULL OR a.floorNumber = :floorNumber) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:bedroomCount IS NULL OR a.bedroomCount = :bedroomCount)")
    Page<Apartment> findWithFilters(
            @Param("buildingId") UUID buildingId,
            @Param("code") String code,         // Tham số mới
            @Param("floorNumber") Integer floorNumber,
            @Param("status") ApartmentStatus status,
            @Param("bedroomCount") Integer bedroomCount,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a FROM Apartment a " +
            "LEFT JOIN a.residents r " +
            "WHERE a.building.id = :buildingId " +
            "AND r.residentType = 'OWNER' " +
            "AND r.movedOutAt IS NULL")
    Page<Apartment> findApartmentsWithOwner(@Param("buildingId") UUID buildingId, Pageable pageable);


    List<Apartment> findByStatus(ApartmentStatus status);
    @Query("SELECT DISTINCT a FROM Apartment a " +
            "JOIN FETCH a.building " +
            "JOIN a.residents r " +
            "WHERE r.user.email = :email " +
            "AND r.movedOutAt IS NULL")
    List<Apartment> findByResidentEmail(@Param("email") String email);

    @Query("SELECT r FROM ApartmentResident r WHERE r.apartment.id = :apartmentId " +
            "AND (:type IS NULL OR r.residentType = :type)")
    Page<ApartmentResident> findHistoryByApartmentId(
            @Param("apartmentId") UUID apartmentId,
            @Param("type") ResidentType type,
            Pageable pageable);
}

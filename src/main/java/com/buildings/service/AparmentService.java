package com.buildings.service;

import com.buildings.dto.response.apartment.ApartmentResponse;
import com.buildings.entity.enums.ApartmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AparmentService {
    ApartmentResponse getById(UUID apartmentId);

    List<ApartmentResponse> getAllApartments();

    List<ApartmentResponse> getByBuildingId(UUID buildingId);

    Page<ApartmentResponse> getByBuildingIdPaged(UUID buildingId, Pageable pageable);

    ApartmentResponse getByBuildingAndCode(UUID buildingId, String code);

    boolean checkExists(UUID buildingId, String code);

    List<ApartmentResponse> getByBuildingAndFloor(UUID buildingId, Integer floorNumber);

    Page<ApartmentResponse> getByBuildingAndStatus(UUID buildingId, ApartmentStatus status, Pageable pageable);

    Page<ApartmentResponse> getByBuildingAndBedrooms(UUID buildingId, Integer bedroomCount, Pageable pageable);

    Long countTotalInBuilding(UUID buildingId);

    Long countByStatusInBuilding(UUID buildingId, ApartmentStatus status);

    Page<ApartmentResponse> searchWithFilters(UUID buildingId, Integer floorNumber, ApartmentStatus status, Integer bedroomCount, Pageable pageable);

    Page<ApartmentResponse> getApartmentsWithOwner(UUID buildingId, Pageable pageable);

    Page<ApartmentResponse> getApartmentsWithoutOwner(UUID buildingId, Pageable pageable);

    void deleteByBuilding(UUID buildingId);
}

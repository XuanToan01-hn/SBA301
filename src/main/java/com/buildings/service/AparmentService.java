package com.buildings.service;

import com.buildings.dto.request.apartment_resident.ApartmentResidentRequest;
import com.buildings.dto.response.apartment.ApartmentResponse;
import com.buildings.dto.response.apartment_resident.ApartmentResidentResponse;
import com.buildings.entity.enums.ApartmentStatus;
import com.buildings.entity.enums.ResidentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AparmentService {

    ApartmentResidentResponse assignResident(ApartmentResidentRequest request);
    ApartmentResponse getById(UUID apartmentId);
    Page<ApartmentResidentResponse> getResidencyHistory(UUID apartmentId, ResidentType type, Pageable pageable);
    List<ApartmentResponse> getAllApartments();
    List<ApartmentResponse> getByBuildingId(UUID buildingId);
    Page<ApartmentResponse> getByBuildingIdPaged(UUID buildingId, Pageable pageable);
    Long countTotalInBuilding(UUID buildingId);
    Long countByStatusInBuilding(UUID buildingId, ApartmentStatus status);
    Page<ApartmentResponse> searchWithFilters(
            UUID buildingId,
            String code,
            Integer floorNumber,
            ApartmentStatus status,
            Integer bedroomCount,
            Pageable pageable
    );
    Page<ApartmentResponse> getApartmentsWithOwner(UUID buildingId, Pageable pageable);
    List<ApartmentResponse> getApartmentsByResidentEmail(String email);
    void moveOut(UUID residentId);
}

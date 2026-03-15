package com.buildings.service;

import com.buildings.dto.BuildingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BuildingService {
    Page<BuildingDTO> searchBuildings(
            String search,
            Boolean apartmentsGenerated,
            Pageable pageable
    );

    BuildingDTO createBuilding(BuildingDTO buildingDTO);

    BuildingDTO updateBuilding(UUID id, BuildingDTO buildingDTO);

    BuildingDTO getBuildingById(UUID id);

    void deleteBuilding(UUID id);

    void generateApartments(UUID buildingId);

    boolean buildingCodeExists(String code);

    boolean buildingCodeExistsExcluding(String code, UUID excludeId);

    boolean buildingNameExists(String name);

    boolean buildingNameExistsExcluding(String name, UUID excludeId);

    BuildingDTO getBuildingByResidentEmail(String email);
}

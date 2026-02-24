package com.buildings.service.impl;

import com.buildings.dto.BuildingDTO;
import com.buildings.entity.Apartment;
import com.buildings.entity.Building;
import com.buildings.entity.enums.ApartmentStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.BuildingMapper;
import com.buildings.repository.ApartmentRepository;
import com.buildings.repository.BuildingRepository;
import com.buildings.service.BuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;
    private final ApartmentRepository apartmentRepository;
    private final BuildingMapper buildingMapper;

    @Override
    public BuildingDTO createBuilding(BuildingDTO buildingDTO) {
        if (buildingRepository.existsByCode(buildingDTO.getCode())) {
            throw new AppException(ErrorCode.BUILDING_CODE_ARE_EXIST);
        }
        if (buildingRepository.existsByName(buildingDTO.getName())) {
            throw new AppException(ErrorCode.BUILDING_NAME_ARE_EXIST);
        }

        Building building = buildingMapper.toEntity(buildingDTO);
        building.setApartmentsGenerated(false);

        building = buildingRepository.save(building);

        // Tòa nhà mới tạo chưa có căn hộ nên count = 0
        return buildingMapper.toDTO(building, 0L);
    }

    @Override
    public BuildingDTO updateBuilding(UUID id, BuildingDTO buildingDTO) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        if (!building.getCode().equals(buildingDTO.getCode()) &&
                buildingRepository.existsByCode(buildingDTO.getCode())) {
            throw new AppException(ErrorCode.BUILDING_CODE_ARE_EXIST);
        }

        if (!building.getName().equals(buildingDTO.getName()) &&
                buildingRepository.existsByName(buildingDTO.getName())) {
            throw new AppException(ErrorCode.BUILDING_NAME_ARE_EXIST);
        }

        if (hasLayoutChanged(building, buildingDTO) && building.isApartmentsGenerated()) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_LAYOUT);
        }

        building.setName(buildingDTO.getName());
        building.setCode(buildingDTO.getCode());
        building.setAddress(buildingDTO.getAddress());
        if (!building.isApartmentsGenerated()) {
            updateBuildingLayout(building, buildingDTO);
        }

        Building savedBuilding = buildingRepository.save(building);
        long count = apartmentRepository.countByBuildingId(id);

        return buildingMapper.toDTO(savedBuilding, count);
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingDTO getBuildingById(UUID id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        long count = apartmentRepository.countByBuildingId(id);
        return buildingMapper.toDTO(building, count);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuildingDTO> getAllBuildings(Pageable pageable) {
        return buildingRepository.findAll(pageable)
                .map(this::mapToDTOWithCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuildingDTO> searchBuildings(String search, Pageable pageable) {
        String searchTerm = (search != null) ? search : "";
        return buildingRepository.findAllWithSearch(searchTerm, pageable)
                .map(this::mapToDTOWithCount);
    }

    @Override
    public void deleteBuilding(UUID id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        if (apartmentRepository.countByBuildingId(id) > 0) {
            throw new AppException(ErrorCode.BUILD_HAS_APARTMENT);
        }

        buildingRepository.delete(building);
    }

    @Override
    public void generateApartments(UUID buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        if (building.isApartmentsGenerated()) {
            throw new AppException(ErrorCode.APARTMENT_ALREADY_GENERATED);
        }

        List<Apartment> apartments = new ArrayList<>();
        for (int floor = 1; floor <= building.getNumFloors(); floor++) {
            int seq = 1;
            // Generate cho từng loại phòng
            seq = addApartments(apartments, building, floor, seq, 1, building.getApartmentsPerFloor1Br(), building.getArea1BrSqm());
            seq = addApartments(apartments, building, floor, seq, 2, building.getApartmentsPerFloor2Br(), building.getArea2BrSqm());
            addApartments(apartments, building, floor, seq, 3, building.getApartmentsPerFloor3Br(), building.getArea3BrSqm());
        }

        apartmentRepository.saveAll(apartments);
        building.setApartmentsGenerated(true);
        buildingRepository.save(building);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildingDTO> getAllBuildings() {
        return buildingRepository.findAll().stream()
                .map(this::mapToDTOWithCount)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuildingDTO> getBuildingsWithoutGeneratedApartments(Pageable pageable) {
        return buildingRepository.findBuildingsWithoutGeneratedApartments(pageable)
                .map(this::mapToDTOWithCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuildingDTO> getBuildingsWithGeneratedApartments(Pageable pageable) {
        return buildingRepository.findBuildingsWithGeneratedApartments(pageable)
                .map(this::mapToDTOWithCount);
    }

    // --- Helper Methods ---

    private BuildingDTO mapToDTOWithCount(Building building) {
        long count = apartmentRepository.countByBuildingId(building.getId());
        return buildingMapper.toDTO(building, count);
    }

    private int addApartments(List<Apartment> list, Building b, int floor, int startSeq, int brCount, int perFloor, double area) {
        for (int i = 0; i < perFloor; i++) {
            list.add(Apartment.builder()
                    .building(b)
                    .code(String.format("%d%02d", floor, startSeq++))
                    .floorNumber(floor)
                    .bedroomCount(brCount)
                    .areaSqm(area)
                    .status(ApartmentStatus.AVAILABLE)
                    .build());
        }
        return startSeq;
    }

    private void updateBuildingLayout(Building e, BuildingDTO d) {
        e.setNumFloors(d.getNumFloors());
        e.setApartmentsPerFloor1Br(d.getApartmentsPerFloor1br());
        e.setApartmentsPerFloor2Br(d.getApartmentsPerFloor2br());
        e.setApartmentsPerFloor3Br(d.getApartmentsPerFloor3br());
        e.setArea1BrSqm(d.getArea1brSqm());
        e.setArea2BrSqm(d.getArea2brSqm());
        e.setArea3BrSqm(d.getArea3brSqm());
    }

    private boolean hasLayoutChanged(Building e, BuildingDTO d) {
        return e.getNumFloors() != d.getNumFloors() ||
                e.getApartmentsPerFloor1Br() != d.getApartmentsPerFloor1br() ||
                e.getApartmentsPerFloor2Br() != d.getApartmentsPerFloor2br() ||
                e.getApartmentsPerFloor3Br() != d.getApartmentsPerFloor3br() ||
                compareBigDecimal(e.getArea1BrSqm(), d.getArea1brSqm()) != 0 ||
                compareBigDecimal(e.getArea2BrSqm(), d.getArea2brSqm()) != 0 ||
                compareBigDecimal(e.getArea3BrSqm(), d.getArea3brSqm()) != 0;
    }

    private int compareBigDecimal(java.math.BigDecimal b1, java.math.BigDecimal b2) {
        if (b1 == null && b2 == null) return 0;
        if (b1 == null || b2 == null) return 1;
        return b1.compareTo(b2);
    }

    @Override public boolean buildingCodeExists(String code) { return buildingRepository.existsByCode(code); }
    @Override public boolean buildingNameExists(String name) { return buildingRepository.existsByName(name); }
    @Override public boolean buildingCodeExistsExcluding(String code, UUID id) { return false; } // Implement tùy Repo
    @Override public boolean buildingNameExistsExcluding(String name, UUID id) { return false; }
}
package com.buildings.mapper;

import com.buildings.dto.BuildingDTO;
import com.buildings.entity.Building;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BuildingMapper {

    @Mapping(target = "currentApartmentCount", source = "apartmentCount")
    @Mapping(target = "apartmentsPerFloor1br", source = "building.apartmentsPerFloor1Br")
    @Mapping(target = "apartmentsPerFloor2br", source = "building.apartmentsPerFloor2Br")
    @Mapping(target = "apartmentsPerFloor3br", source = "building.apartmentsPerFloor3Br")
    @Mapping(target = "area1brSqm", source = "building.area1BrSqm")
    @Mapping(target = "area2brSqm", source = "building.area2BrSqm")
    @Mapping(target = "area3brSqm", source = "building.area3BrSqm")
    BuildingDTO toDTO(Building building, long apartmentCount);

    @Mapping(target = "currentApartmentCount", ignore = true)
    @Mapping(target = "apartmentsPerFloor1br", source = "apartmentsPerFloor1Br")
    @Mapping(target = "apartmentsPerFloor2br", source = "apartmentsPerFloor2Br")
    @Mapping(target = "apartmentsPerFloor3br", source = "apartmentsPerFloor3Br")
    @Mapping(target = "area1brSqm", source = "area1BrSqm")
    @Mapping(target = "area2brSqm", source = "area2BrSqm")
    @Mapping(target = "area3brSqm", source = "area3BrSqm")
    BuildingDTO toDTO(Building building);

    @Mapping(target = "apartmentsPerFloor1Br", source = "apartmentsPerFloor1br")
    @Mapping(target = "apartmentsPerFloor2Br", source = "apartmentsPerFloor2br")
    @Mapping(target = "apartmentsPerFloor3Br", source = "apartmentsPerFloor3br")
    @Mapping(target = "area1BrSqm", source = "area1brSqm")
    @Mapping(target = "area2BrSqm", source = "area2brSqm")
    @Mapping(target = "area3BrSqm", source = "area3brSqm")
    Building toEntity(BuildingDTO dto);
}

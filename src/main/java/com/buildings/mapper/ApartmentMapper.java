package com.buildings.mapper;


import com.buildings.dto.response.apartment.ApartmentResponse;
import com.buildings.entity.Apartment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApartmentMapper {

    @Mapping(source = "building.name", target = "buildingName")
    @Mapping(source = "building.id", target = "buildingId")
    ApartmentResponse toResponse(Apartment apartment);

    List<ApartmentResponse> toResponseList(List<Apartment> apartments);
}
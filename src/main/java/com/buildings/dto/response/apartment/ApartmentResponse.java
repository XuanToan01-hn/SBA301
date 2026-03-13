package com.buildings.dto.response.apartment;

import com.buildings.dto.response.apartment_resident.ApartmentResidentResponse;
import com.buildings.entity.enums.ApartmentStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ApartmentResponse {

    private UUID id;
    private String code;
    private int floorNumber;
    private double areaSqm;
    private int bedroomCount;
    private ApartmentStatus status;

    private String buildingName;
    private UUID buildingId;
    private List<ApartmentResidentResponse> residents;
}

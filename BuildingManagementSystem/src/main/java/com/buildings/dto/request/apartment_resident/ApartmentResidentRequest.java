package com.buildings.dto.request.apartment_resident;


import com.buildings.entity.enums.ResidentType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentResidentRequest {
    @NotNull(message = "USER_ID_REQUIRED")
    private UUID userId;

    @NotNull(message = "APARTMENT_ID_REQUIRED")
    private UUID apartmentId;

    @NotNull(message = "RESIDENT_TYPE_REQUIRED")
    private ResidentType residentType;

    private String idCardNumber;
    private String contractDetails;
    private String ownershipCertificate;
    private String legalDocs;
    private String note;
}
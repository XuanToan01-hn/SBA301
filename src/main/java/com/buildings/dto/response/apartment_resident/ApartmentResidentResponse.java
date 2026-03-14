package com.buildings.dto.response.apartment_resident;


import com.buildings.entity.enums.ResidentType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentResidentResponse {
    private UUID id;
    private UUID apartmentId;
    private String apartmentCode;
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String residentType;
    private LocalDateTime assignedAt;
    private boolean isCurrent;
}
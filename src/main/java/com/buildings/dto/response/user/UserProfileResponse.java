package com.buildings.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String fullName;
    private String email;
    private String phone;
    private String role; // Lấy từ UserRole
    private String buildingName;
    private String apartmentCode;
    private Integer floorNumber;
}
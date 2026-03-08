package com.buildings.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentRequest {
    private UUID buildingId;      // ID tòa nhà (null nếu là Admin tổng)
    private List<String> roleCodes; // Danh sách Role: ["MANAGER", "STAFF"]
}

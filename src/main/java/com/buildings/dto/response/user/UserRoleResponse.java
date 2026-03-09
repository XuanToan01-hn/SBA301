package com.buildings.dto.response.user;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResponse {
    private String roleCode;
    private String roleName;
    private UUID buildingId;
    private String buildingName; // Thêm tên tòa nhà để FE hiển thị luôn, không cần gọi API khác
}
package com.buildings.dto.response.user;

import com.buildings.entity.enums.UserStatus;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private UserStatus status;
    private List<UserRoleResponse> roles;
}
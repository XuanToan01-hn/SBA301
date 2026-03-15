package com.buildings.dto.request.user;

import com.buildings.entity.enums.UserStatus;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    UUID id;
    String fullName;
    String email;
    @Size(min = 6, message = "PASSWORD_INVALID")
    String password;
    String phone;
    UserStatus status;
    List<RoleAssignmentRequest> assignments;
}

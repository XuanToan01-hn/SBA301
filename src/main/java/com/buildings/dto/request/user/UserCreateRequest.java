package com.buildings.dto.request.user;

import com.buildings.entity.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    private String fullName;

    private String email;

    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private List<RoleAssignmentRequest> assignments;
}

package com.buildings.dto.response.user;

import com.buildings.entity.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
}


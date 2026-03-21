package com.buildings.dto.request.user;

import com.buildings.entity.enums.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @NotNull(message = "ID người dùng không được để trống")
    UUID id;

    @NotNull(message = "Tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    String fullName;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    String email;

    @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự")
    String password;

    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Số điện thoại không hợp lệ (phải là số Việt Nam)"
    )
    String phone;

    UserStatus status;

    @Valid
    List<RoleAssignmentRequest> assignments;
}
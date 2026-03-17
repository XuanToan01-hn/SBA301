package com.buildings.service;

import com.buildings.dto.request.user.UserCreateRequest;
import com.buildings.dto.request.user.UserUpdateRequest;
import com.buildings.dto.response.user.UserProfileResponse;
import com.buildings.dto.response.user.UserResponse;
import com.buildings.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(UserCreateRequest userCreateRequest);
    UserResponse updateUser(UserUpdateRequest userUpdateRequest);
    void deleteUser(String userId);
    UserProfileResponse getMyProfile();
    UserResponse getUserById(UUID userId);
    List<UserResponse> searchUsers(String query);
    Page<UserResponse> searchUser(String keyword, UserStatus status, int page, int size);
}

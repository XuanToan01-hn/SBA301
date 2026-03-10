package com.buildings.service;

import com.buildings.dto.request.user.UserCreateRequest;
import com.buildings.dto.request.user.UserUpdateRequest;
import com.buildings.dto.response.user.UserProfileResponse;
import com.buildings.dto.response.user.UserResponse;
import org.springframework.data.domain.Page;

public interface UserService {

    UserResponse createUser(UserCreateRequest userCreateRequest);
    UserResponse updateUser(UserUpdateRequest userUpdateRequest);
    UserResponse deleteUser(String userId);
    Page<UserResponse> searchUser(String name, int page, int size);
    UserProfileResponse getMyProfile();
}

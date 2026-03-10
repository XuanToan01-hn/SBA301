package com.buildings.service.impl;

import com.buildings.dto.request.user.UserCreateRequest;
import com.buildings.dto.request.user.UserUpdateRequest;
import com.buildings.dto.response.user.UserProfileResponse;
import com.buildings.dto.response.user.UserResponse;
import com.buildings.entity.Apartment;
import com.buildings.entity.User;
import com.buildings.repository.ApartmentResidentRepository;
import com.buildings.repository.UserRepository;
import com.buildings.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApartmentResidentRepository residentRepository;

    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        return null;
    }

    @Override
    public UserResponse updateUser(UserUpdateRequest userUpdateRequest) {
        return null;
    }

    @Override
    public UserResponse deleteUser(String userId) {
        return null;
    }

    @Override
    public Page<UserResponse> searchUser(String name, int page, int size) {
        return null;
    }

    @Override
    public UserProfileResponse getMyProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Auth name: {}", authentication.getName());
        log.info("Principal: {}", authentication.getPrincipal());
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("User: {}", user.getId());

        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone());

        // 4. Lấy Role (Ví dụ lấy cái đầu tiên trong danh sách)
        if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
            builder.role(user.getUserRoles().get(0).getRole().getName());
        }

        residentRepository.findFirstByUserIdAndMovedOutAtIsNullOrderByAssignedAtDesc(user.getId())
                .ifPresent(resident -> {
                    Apartment apt = resident.getApartment();
                    if (apt != null) {
                        builder.apartmentCode(apt.getCode());
                        builder.floorNumber(apt.getFloorNumber());
                        if (apt.getBuilding() != null) {
                            builder.buildingName(apt.getBuilding().getName());
                        }
                    }
                });

        return builder.build();
    }
}

package com.buildings.service.impl;

import com.buildings.dto.request.user.UserCreateRequest;
import com.buildings.dto.request.user.UserUpdateRequest;
import com.buildings.dto.response.user.UserProfileResponse;
import com.buildings.dto.response.user.UserResponse;
import com.buildings.dto.response.user.UserRoleResponse;
import com.buildings.entity.*;
import com.buildings.entity.enums.UserStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.repository.ApartmentResidentRepository;
import com.buildings.repository.UserRepository;
import com.buildings.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApartmentResidentRepository residentRepository;
    private final PasswordEncoder passwordEncoder;


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

    private UserResponse mapToUserResponse(User user) {
        List<UserRoleResponse> roleResponses = user.getUserRoles() == null ? List.of() :
                user.getUserRoles().stream()
                        .map(ur -> UserRoleResponse.builder()
                                .roleName(ur.getRole().getName())
                                .roleCode(ur.getRole().getCode())
                                .buildingName(ur.getBuilding() != null ? ur.getBuilding().getName() : null)
                                .build())
                        .toList();

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roles(roleResponses)
                .build();
    }
    @Override
    public List<UserResponse> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // Tìm kiếm từ DB
        List<User> users = userRepository.searchUsers(query.trim());

        // Map sang DTO bằng hàm thủ công có sẵn của bạn
        return users.stream()
                .map(this::mapToUserResponse)
                .toList();
    }


}

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
import com.buildings.mapper.UserMapper;
import com.buildings.repository.*;
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
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final BuildingRepository buildingRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    @Override
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setUserRoles(new ArrayList<>());

        User savedUser = userRepository.save(user);

        if (request.getAssignments() != null) {
            request.getAssignments().forEach(assignment -> {
                Building building = (assignment.getBuildingId() != null)
                        ? buildingRepository.findById(assignment.getBuildingId()).orElse(null)
                        : null;

                assignment.getRoleCodes().forEach(code -> {
                    Role role = roleRepository.findByCode(code)
                            .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

                    UserRole userRole = UserRole.builder()
                            .user(savedUser)
                            .role(role)
                            .building(code.equals("ADMIN") ? null : building)
                            .build();
                    userRoleRepository.save(userRole);
                    savedUser.getUserRoles().add(userRole);
                });
            });
        }

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UserUpdateRequest request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Cập nhật thông tin cơ bản qua mapper hoặc thủ công
        userMapper.updateUser(user, request);

        // 3. Nếu có cập nhật password (chỉ khi request có pass mới)
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 4. Lưu và map lại kết quả
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Thay vì xóa khỏi DB, chúng ta chuyển trạng thái
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("User {} has been deactivated", userId);
    }

    @Override
    public Page<UserResponse> searchUser(String keyword, UserStatus status, int page, int size) {
        // Tạo đối tượng phân trang, sắp xếp theo tên mặc định
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("fullName").ascending());

        Page<User> userPage = userRepository.findAllWithFilter(keyword, status, pageable);

        // Map từ Page<User> sang Page<UserResponse>
        return userPage.map(this::mapToUserResponse);
    }

    // Hàm bổ sung để lấy chi tiết 1 user
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return mapToUserResponse(user);
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

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return mapToUserResponse(user);
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

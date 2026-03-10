package com.buildings.service.impl;

import com.buildings.configuration.JwtProvider;
import com.buildings.dto.request.Auth.AuthenticationRequest;
import com.buildings.dto.request.user.UserCreateRequest;
import com.buildings.dto.response.Auth.AuthenticationResponse;
import com.buildings.dto.response.user.UserResponse;
import com.buildings.entity.Building;
import com.buildings.entity.Role;
import com.buildings.entity.User;
import com.buildings.entity.UserRole;
import com.buildings.entity.enums.UserStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.UserMapper;
import com.buildings.repository.BuildingRepository;
import com.buildings.repository.RoleRepository;
import com.buildings.repository.UserRepository;
import com.buildings.repository.UserRoleRepository;
import com.buildings.service.AuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final BuildingRepository buildingRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public UserResponse signup(UserCreateRequest request) {
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
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_EMAIL_PASSWORD);
        }

        String accessToken = jwtProvider.generateToken(user);

        return AuthenticationResponse.builder()
                .token(accessToken)
                .authenticated(true)
                .build();
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            jwtProvider.verifyToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
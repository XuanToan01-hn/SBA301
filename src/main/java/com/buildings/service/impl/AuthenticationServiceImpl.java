//package com.buildings.service.impl;//package com.buildings.service.impl;
//
// import com.buildings.configuration.JwtTokenProvider;
// import com.buildings.dto.request.Auth.AuthenticationRequest;
// import com.buildings.dto.request.user.UserCreateRequest;
// import com.buildings.dto.response.Auth.AuthenticationResponse;
// import com.buildings.dto.response.user.UserResponse;
// import com.buildings.entity.User;
// import com.buildings.entity.UserRole;
// import com.buildings.entity.enums.UserStatus;
// import com.buildings.exception.AppException;
// import com.buildings.exception.ErrorCode;
// import com.buildings.repository.RoleRepository;
// import com.buildings.repository.UserRepository;
// import com.buildings.repository.UserRoleRepository;
// import com.buildings.service.AuthenticationService;
//
// import jakarta.transaction.Transactional;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
//
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;
//
// import com.buildings.entity.Role;
//
//
//@Service
// @Slf4j
// public class AuthenticationServiceImpl implements AuthenticationService {
//
//     @Autowired
//     private UserRepository userRepository;
//
//     @Autowired
//     private RoleRepository roleRepository;
//
//     @Autowired
//     private UserRoleRepository userRoleRepository; // Cần thêm repo này
//
//     @Autowired
//     private PasswordEncoder passwordEncoder;
//
//     @Autowired
//     private JwtTokenProvider jwtTokenProvider;
//
//
//         @Override
//         @Transactional
//         public UserResponse signup(UserCreateRequest request) {
//             if (userRepository.findByEmailWithRoles(request.getEmail()).isPresent()) {
//                 throw new AppException(ErrorCode.USER_EXISTED);
//             }
//
//             User user = User.builder()
//                     .fullName(request.getFullName())
//                     .email(request.getEmail())
//                     .password(passwordEncoder.encode(request.getPassword()))
//                     .phone(request.getPhone())
//                     .status(UserStatus.ACTIVE)
//                     .build();
//
//             User savedUser = userRepository.save(user);
//
//             if (request.getRoles() != null) {
//                 for (String roleCode : request.getRoles()) {
//                     Role role = roleRepository.findByCode(roleCode)
//                             .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
//
//                     UserRole userRole = UserRole.builder()
//                             .user(savedUser)
//                             .role(role)
//                             .building(null) // Mặc định signup chưa có building, hoặc set tùy logic
//                             .build();
//                     userRoleRepository.save(userRole);
//                 }
//             }
//
//             return UserResponse.builder()
//                     .fullName(savedUser.getFullName())
//                     .email(savedUser.getEmail())
//                     .status(savedUser.getStatus())
//                     .build();
//         }
//
//
//     @Override
//     public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
//         PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
//
//         User user = userRepository.findByEmailWithRoles(authenticationRequest.getEmail())
//                 .orElseThrow(() ->
//                         new AppException(ErrorCode.USER_NOT_EXISTED));
//
//         boolean matches = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
//         if(!matches) {
//             throw new AppException(ErrorCode.INVALID_EMAIL_PASSWORD);
//         }
//
//         String accessToken = jwtTokenProvider.generateToken(user.getEmail());
//
//         log.info("User {} logged in successfully with role {}", user.getFullName());
//
//         return AuthenticationResponse.builder()
//                 .token(accessToken)
//                 .authenticated(true)
//                 .build();
//     }

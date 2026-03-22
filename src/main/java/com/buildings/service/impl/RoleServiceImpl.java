package com.buildings.service.impl;

import com.buildings.dto.response.role.RoleResponse;
import com.buildings.entity.Role;
import com.buildings.repository.RoleRepository;
import com.buildings.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();

        // Mapping thủ công bằng tay (Manual Mapping)
        return roles.stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    // Hàm helper để map thủ công từ Entity sang DTO
    private RoleResponse mapToRoleResponse(Role role) {
        if (role == null) return null;

        return RoleResponse.builder()
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
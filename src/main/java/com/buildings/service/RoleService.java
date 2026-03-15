package com.buildings.service;

import com.buildings.dto.response.role.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> getAllRoles();
}
package com.buildings.mapper;

import com.buildings.dto.request.user.UserCreateRequest;
import com.buildings.dto.response.user.UserResponse;
import com.buildings.dto.response.user.UserRoleResponse;
import com.buildings.entity.User;
import com.buildings.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest request);

    @Mapping(source = "userRoles", target = "roles") // Map từ userRoles (Entity) sang roles (DTO)
    UserResponse toUserResponse(User user);

    @Mapping(source = "role.code", target = "roleCode")
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "building.id", target = "buildingId")
    @Mapping(source = "building.name", target = "buildingName")
    UserRoleResponse toUserRoleResponse(UserRole userRole);
}
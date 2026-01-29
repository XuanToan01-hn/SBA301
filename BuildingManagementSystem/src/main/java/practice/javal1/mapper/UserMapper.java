
package practice.javal1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import practice.javal1.dto.request.user.UserCreateRequest;
import practice.javal1.dto.request.user.UserUpdateRequest;
import practice.javal1.dto.response.user.UserResponse;
import practice.javal1.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(target = "role", ignore = true)
    User toUser(UserCreateRequest request);

    @Mapping(source = "role.code", target = "code")
    UserResponse toUserResponse(User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);


}
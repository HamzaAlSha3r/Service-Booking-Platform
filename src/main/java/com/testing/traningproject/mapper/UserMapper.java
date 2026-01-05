package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.UserResponse;
import com.testing.traningproject.model.entity.Role;
import com.testing.traningproject.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct Mapper for User entity to UserResponse DTO
 *
 * Automatically generates mapping code at compile time
 * No manual mapping needed - just add fields to entity/DTO and MapStruct handles it!
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convert User entity to UserResponse DTO
     *
     * @param user User entity
     * @return UserResponse DTO
     */
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toResponse(User user);

    /**
     * Convert list of Users to list of UserResponses
     *
     * @param users List of User entities
     * @return List of UserResponse DTOs
     */
    List<UserResponse> toResponseList(List<User> users);

    /**
     * Custom mapping for roles (Set<Role> → Set<String>)
     *
     * @param roles Set of Role entities
     * @return Set of role names as strings
     */
    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}


package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Mapper interface for converting between {@link User} entities and {@link UserDto} objects.
 *
 * <p>Implementations are generated automatically by MapStruct at compile time. This mapper is
 * configured as a Spring component for dependency injection.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

  /**
   * Converts a {@link UserDto} to a {@link User} entity.
   *
   * @param userRequest the user DTO containing request data
   * @return the corresponding {@link User} entity
   */
  User toEntity(UserDto userRequest);

  /**
   * Converts a {@link User} entity to a {@link UserDto}.
   *
   * @param user the user entity
   * @return the corresponding {@link UserDto}
   */
  UserDto toDto(User user);

  /**
   * Converts a list of {@link User} entities to a list of {@link UserDto} objects.
   *
   * @param users the list of user entities
   * @return a list of corresponding {@link UserDto} objects
   */
  List<UserDto> toDtoList(List<User> users);
}

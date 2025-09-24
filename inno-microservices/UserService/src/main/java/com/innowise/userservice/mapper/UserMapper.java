package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.CreateUserRequest;
import com.innowise.userservice.model.dto.UpdateUserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import com.innowise.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
  User toEntity(CreateUserRequest userRequest);

  User toEntity(UpdateUserRequest userRequest);

  UserResponse toDto(User user);

  List<UserResponse> toDtoList(List<User> users);
}

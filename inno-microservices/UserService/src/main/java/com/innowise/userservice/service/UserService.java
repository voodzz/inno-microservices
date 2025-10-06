package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.UserDto;

/**
 * Service interface for managing users. Extends {@link CrudService} to provide standard CRUD
 * operations for {@link UserDto}.
 */
public interface UserService extends CrudService<UserDto, Long> {

  /**
   * Finds a user by their email address.
   *
   * @param email the email address of the user
   * @return the {@link UserDto} if found
   */
  UserDto findByEmail(String email);
}

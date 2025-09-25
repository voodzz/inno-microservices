package com.innowise.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User")
public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(Long id) {
    super("User with ID '" + id + "' not found");
  }

  public UserNotFoundException(String email) {
    super("User with email '" + email + "' not found");
  }
}

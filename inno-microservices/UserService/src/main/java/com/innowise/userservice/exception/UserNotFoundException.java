package com.innowise.userservice.exception;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(Long id) {
    super("User with ID '" + id + "' not found");
  }

  public UserNotFoundException(String email) {
    super("User with email '" + email + "' not found");
  }
}

package com.innowise.userservice.exception;

public class UserUpateException extends RuntimeException {
  public UserUpateException(Long id) {
    super("Update failed for user with id " + id);
  }
}

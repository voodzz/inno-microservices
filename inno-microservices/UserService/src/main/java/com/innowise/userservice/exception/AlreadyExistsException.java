package com.innowise.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Such object of entity already exists")
public class AlreadyExistsException extends RuntimeException {
  @Serial private static final long serialVersionUID = 3317443501033478991L;

  public AlreadyExistsException() {}

  public AlreadyExistsException(String message) {
    super(message);
  }

  public AlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public AlreadyExistsException(Throwable cause) {
    super(cause);
  }
}

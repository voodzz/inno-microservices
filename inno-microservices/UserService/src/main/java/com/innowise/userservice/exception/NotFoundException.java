package com.innowise.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Entity not found")
public class NotFoundException extends RuntimeException {

  @Serial private static final long serialVersionUID = 2283290493219293067L;
  private static final String MESSAGE_TEMP = "Entity with id '%d' not found";

  public NotFoundException() {}

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(Long id) {
    super(MESSAGE_TEMP.formatted(id));
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotFoundException(Throwable cause) {
    super(cause);
  }
}

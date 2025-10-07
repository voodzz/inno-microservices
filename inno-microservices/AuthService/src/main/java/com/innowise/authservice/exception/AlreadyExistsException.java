package com.innowise.authservice.exception;

import java.io.Serial;

public class AlreadyExistsException extends RuntimeException {

  @Serial private static final long serialVersionUID = -3543805809029881259L;

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

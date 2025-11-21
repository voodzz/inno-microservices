package com.innowise.paymentservice.exception;

import java.io.Serial;

public class AlreadyExistsException extends RuntimeException {
  @Serial private static final long serialVersionUID = -4014404881652484564L;

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

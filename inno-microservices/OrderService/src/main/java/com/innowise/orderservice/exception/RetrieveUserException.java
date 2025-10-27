package com.innowise.orderservice.exception;

import java.io.Serial;

public class RetrieveUserException extends RuntimeException {
  @Serial private static final long serialVersionUID = -3782361036625980740L;

  public RetrieveUserException() {}

  public RetrieveUserException(String message) {
    super(message);
  }

  public RetrieveUserException(String message, Throwable cause) {
    super(message, cause);
  }

  public RetrieveUserException(Throwable cause) {
    super(cause);
  }
}

package com.innowise.orderservice.exception;

import java.io.Serial;

/** Exception thrown when credentials from the request are not the same as from another service */
public class CredentialsMismatchException extends RuntimeException {
  @Serial private static final long serialVersionUID = -743165464135065648L;

  public CredentialsMismatchException() {}

  public CredentialsMismatchException(String message) {
    super(message);
  }

  public CredentialsMismatchException(String message, Throwable cause) {
    super(message, cause);
  }

  public CredentialsMismatchException(Throwable cause) {
    super(cause);
  }
}

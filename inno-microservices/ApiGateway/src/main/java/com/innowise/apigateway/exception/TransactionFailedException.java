package com.innowise.apigateway.exception;

import java.io.Serial;

/**
 * Custom runtime exception thrown when a multiservice transaction fails and requires a rollback.
 * This is used to signal a composite failure during operations like user registration.
 */
public class TransactionFailedException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1032498112934032841L;

  public TransactionFailedException() {}

  public TransactionFailedException(String message) {
    super(message);
  }

  public TransactionFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public TransactionFailedException(Throwable cause) {
    super(cause);
  }
}

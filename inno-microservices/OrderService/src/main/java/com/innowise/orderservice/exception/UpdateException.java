package com.innowise.orderservice.exception;

import java.io.Serial;

public class UpdateException extends RuntimeException {
  private static final String MESSAGE_TEMP = "Update failed for entity with id '%d'";
  @Serial private static final long serialVersionUID = -2013232181406717713L;

  public UpdateException() {}

  public UpdateException(String message) {
    super(message);
  }

  public UpdateException(Long id) {
    super(MESSAGE_TEMP.formatted(id));
  }

  public UpdateException(Long id, Throwable cause) {
    super(MESSAGE_TEMP.formatted(id), cause);
  }

  public UpdateException(String message, Throwable cause) {
    super(message, cause);
  }

  public UpdateException(Throwable cause) {
    super(cause);
  }
}

package com.innowise.authservice.exception;

import java.io.Serial;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>This is an unchecked exception (it extends {@link RuntimeException}) and is typically used by
 * service or repository layers to indicate that an entity does not exist.
 */
public class NotFoundException extends RuntimeException {
  @Serial private static final long serialVersionUID = -8708306313734547443L;

  public NotFoundException() {}

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotFoundException(Throwable cause) {
    super(cause);
  }
}

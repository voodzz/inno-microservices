package com.innowise.authservice.exception;

import java.io.Serial;

/**
 * Exception thrown to indicate that an attempt to create or register a resource failed because an
 * entity with the same unique data already exists.
 *
 * <p>This is an unchecked exception (it extends {@link RuntimeException}) and can be thrown from
 * service or controller layers to signal duplicate-creation errors.
 */
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

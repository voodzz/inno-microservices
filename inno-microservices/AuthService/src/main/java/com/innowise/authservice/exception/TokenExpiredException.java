package com.innowise.authservice.exception;

import java.io.Serial;

/**
 * Exception thrown when an authentication token has expired or is otherwise no longer valid for
 * use.
 */
public class TokenExpiredException extends RuntimeException {

  @Serial private static final long serialVersionUID = -6373899341168081618L;

  public TokenExpiredException() {}

  public TokenExpiredException(String message) {
    super(message);
  }

  public TokenExpiredException(String message, Throwable cause) {
    super(message, cause);
  }

  public TokenExpiredException(Throwable cause) {
    super(cause);
  }
}

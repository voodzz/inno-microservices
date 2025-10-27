package com.innowise.orderservice.exception;

import java.io.Serial;

/**
 * Exception thrown when the circuit breaker is open and the service is unavailable.
 *
 * <p>This exception is used to indicate that the User Service is currently unavailable
 * due to the circuit breaker being in an open state. It allows for graceful error handling
 * and provides informative error messages to the client.
 */
public class CircuitBreakerOpenException extends RuntimeException {
  @Serial private static final long serialVersionUID = -1234567890123456789L;

  public CircuitBreakerOpenException() {}

  public CircuitBreakerOpenException(String message) {
    super(message);
  }

  public CircuitBreakerOpenException(String message, Throwable cause) {
    super(message, cause);
  }

  public CircuitBreakerOpenException(Throwable cause) {
    super(cause);
  }
}


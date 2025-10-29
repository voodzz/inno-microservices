package com.innowise.apigateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Global exception handler for the API Gateway, providing structured error responses. Uses
 * {@code @RestControllerAdvice} to catch exceptions across all controllers.
 */
@RestControllerAdvice
public class CustomExceptionHandler {
  /**
   * Handles general, unhandled {@link Exception}.
   *
   * @param ex The caught Exception.
   * @return A Mono emitting a ResponseEntity with an HTTP 500 Internal Server Error status and a
   *     map containing the error message and timestamp.
   */
  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Map<String, String>>> handleGeneralException(Exception ex) {
    Map<String, String> body =
        Map.of(
            "error", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString());
    return Mono.just(ResponseEntity.internalServerError().body(body));
  }

  /**
   * Handles {@link TransactionFailedException}s, typically from multiservice registration
   * failures.
   *
   * @param ex The caught TransactionFailedException.
   * @return A Mono emitting a ResponseEntity with an HTTP 500 Internal Server Error status and a
   *     map containing a generic registration failed error and the exception message.
   */
  @ExceptionHandler(TransactionFailedException.class)
  public Mono<ResponseEntity<Map<String, String>>> handleTransactionFailedException(
      TransactionFailedException ex) {
    Map<String, String> body = Map.of("error", "Registration failed", "message", ex.getMessage());
    return Mono.just(ResponseEntity.internalServerError().body(body));
  }
}

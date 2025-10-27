package com.innowise.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception handler for REST controllers.
 *
 * <p>This class intercepts exceptions converts them into appropriate HTTP responses with error
 * details.
 */
@RestControllerAdvice
public class CustomExceptionHandler {
  private static final String ERROR = "Error";
  private static final String TIMESTAMP = "Timestamp";
  private static final String CAUSE = "cause";

  /**
   * Handles all uncaught exceptions that are not specifically handled elsewhere.
   *
   * @param ex the exception that was thrown
   * @return a {@link ResponseEntity} with status 500 (Internal Server Error) and the exception
   *     message as the response body
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleGeneralException(Exception ex) {
    return ResponseEntity.internalServerError().body(ex.getMessage());
  }

  /**
   * Handles validation errors when request body arguments fail constraints.
   *
   * @param ex the {@link MethodArgumentNotValidException} containing validation errors
   * @return a {@link ResponseEntity} with status 400 (Bad Request) and a map of field names to
   *     validation messages as the response body
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleNotValidException(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errors);
  }

  /**
   * Handles custom {@link UpdateException} errors.
   *
   * @param ex the exception indicating a failed update operation
   * @return a {@link ResponseEntity} with status 400 (Bad Request) and a map containing the error
   *     message and a timestamp
   */
  @ExceptionHandler(UpdateException.class)
  public ResponseEntity<Map<String, String>> handleUpdateException(RuntimeException ex) {
    Map<String, String> error = new HashMap<>();
    error.put(ERROR, ex.getMessage());
    error.put(TIMESTAMP, LocalDateTime.now().toString());
    return ResponseEntity.badRequest().body(error);
  }

  /**
   * Handles custom {@link RetrieveUserException} errors.
   *
   * @param ex the exception indicating failed retrieval of a user
   * @return a {@link ResponseEntity} with status 500 (Internal Server Error) and a map containing
   *     error details
   */
  @ExceptionHandler(RetrieveUserException.class)
  public ResponseEntity<Map<String, Object>> handleRetrieveUserException(RetrieveUserException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put(TIMESTAMP, LocalDateTime.now());
    body.put(CAUSE, ex.getCause());
    body.put(ERROR, ex.getMessage());
    return ResponseEntity.internalServerError().body(body);
  }

  /**
   * Handles custom {@link NotFoundException} errors.
   *
   * @param ex the exception indicating that such user is not present
   * @return a {@link ResponseEntity} with status 404 (Not Found) and a map containing the error
   *     message and a timestamp
   */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
    Map<String, String> error = new HashMap<>();
    error.put(ERROR, ex.getMessage());
    error.put(TIMESTAMP, LocalDateTime.now().toString());
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles custom {@link CircuitBreakerOpenException} errors.
   *
   * @param ex the exception thrown
   * @return a {@link ResponseEntity} with status 500 (Internal Server Error) and a map containing a
   *     timestamp and the cause
   */
  @ExceptionHandler(CircuitBreakerOpenException.class)
  public ResponseEntity<Map<String, Object>> handleCircuitBreakerOpenException(
      CircuitBreakerOpenException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put(TIMESTAMP, LocalDateTime.now().toString());
    body.put(CAUSE, ex.getCause());
    return ResponseEntity.internalServerError().body(body);
  }
}

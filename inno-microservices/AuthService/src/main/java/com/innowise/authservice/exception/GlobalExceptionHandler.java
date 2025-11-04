package com.innowise.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that centralizes exception-to-HTTP-response mapping for controllers.
 *
 * <p>Annotated with {@code @RestControllerAdvice} so that the return values of handler methods are
 * written directly to the HTTP response body (as JSON for complex types like {@link Map}).
 *
 * <p>Handler methods in this class translate common exceptions into appropriate HTTP status codes:
 *
 * <ul>
 *   <li>{@link Exception} &rarr; 500 Internal Server Error
 *   <li>{@link MethodArgumentNotValidException} &rarr; 400 Bad Request (field -> message map)
 *   <li>{@link AlreadyExistsException} &rarr; 409 Conflict
 *   <li>{@link TokenExpiredException} &rarr; 403 Forbidden
 *   <li>{@link NotFoundException} &rarr; 404 Not Found
 *   <li>{@link TransactionFailedException} &rarr; 505 Internal Server Error
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final String ERROR = "Error";
  private static final String CAUSE = "Cause";
  private static final String MESSAGE = "Message";

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleGeneralException(Exception ex) {
    return ResponseEntity.internalServerError().body(ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleNotValidException(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(AlreadyExistsException.class)
  public ResponseEntity<String> handleAlreadyExistsException(AlreadyExistsException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
  }

  @ExceptionHandler(TokenExpiredException.class)
  public ResponseEntity<String> handleTokenExpiredException(TokenExpiredException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<String> handleNotFoundException(NotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(TransactionFailedException.class)
  public ResponseEntity<Map<String, String>> handleTransactionFailedException(
      TransactionFailedException ex) {
    Map<String, String> body =
        Map.of(
            ERROR, "Registration failed",
            CAUSE, ex.getCause().getMessage(),
            MESSAGE, ex.getMessage());
    return ResponseEntity.internalServerError().body(body);
  }
}

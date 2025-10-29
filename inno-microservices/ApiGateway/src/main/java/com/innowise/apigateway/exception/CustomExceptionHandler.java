package com.innowise.apigateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {
  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Map<String, String>>> handleGeneralException(Exception ex) {
    Map<String, String> body =
        Map.of(
            "error", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString());
    return Mono.just(ResponseEntity.internalServerError().body(body));
  }

  @ExceptionHandler(TransactionFailedException.class)
  public Mono<ResponseEntity<Map<String, String>>> handleTransactionFailedException(
      TransactionFailedException ex) {
    Map<String, String> body = Map.of("error", "Registration failed", "message", ex.getMessage());
    return Mono.just(ResponseEntity.internalServerError().body(body));
  }
}

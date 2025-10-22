package com.innowise.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
  private static final String SERVICE = "service";
  private static final String ERROR = "error";
  private static final String CAUSE = "Service unavailable";
  private static final String MESSAGE = "message";
  private static final String MESSAGE_TEMP = "%s is currently unavailable. Please try again later";
  private static final String AUTH_SERVICE = "AuthService";
  private static final String USER_SERVICE = "UserService";
  private static final String ORDER_SERVICE = "OrderService";

  @GetMapping("/auth")
  public Mono<ResponseEntity<Map<String, String>>> authServiceFallback() {
    return Mono.just(
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(getBody(AUTH_SERVICE)));
  }

  @GetMapping("/users")
  public Mono<ResponseEntity<Map<String, String>>> userServiceFallback() {
    return Mono.just(
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(getBody(USER_SERVICE)));
  }

  @GetMapping("/orders")
  public Mono<ResponseEntity<Map<String, String>>> orderServiceFallback() {
    return Mono.just(
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(getBody(ORDER_SERVICE)));
  }

  private Map<String, String> getBody(String service) {
    return Map.of(SERVICE, service, ERROR, CAUSE, MESSAGE, MESSAGE_TEMP.formatted(service));
  }
}

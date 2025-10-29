package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.AuthResponse;
import com.innowise.apigateway.dto.RegistrationRequest;
import com.innowise.apigateway.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for handling new user registration requests at the {@code /api/v1/auth/register}
 * endpoint.
 */
@RestController
@RequiredArgsConstructor
public class RegistrationController {
  private final RegistrationService registrationService;

  /**
   * Handles the POST request for new user registration.
   *
   * @param request The {@link RegistrationRequest} containing user and card data.
   * @return A Mono emitting a ResponseEntity with the {@link AuthResponse} (containing a JWT) upon
   *     successful registration and authentication.
   */
  @PostMapping("/api/v1/auth/register")
  public Mono<ResponseEntity<Void>> register(@RequestBody RegistrationRequest request) {
    return registrationService.registerUser(request).thenReturn(ResponseEntity.ok().build());
  }
}

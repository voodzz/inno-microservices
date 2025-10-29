package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.AuthResponse;
import com.innowise.apigateway.dto.UserDto;
import com.innowise.apigateway.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RegistrationController {
  private final RegistrationService registrationService;

  @PostMapping("/api/v1/auth/register")
  public Mono<ResponseEntity<AuthResponse>> register(@RequestBody UserDto request) {
    return registrationService.registerUser(request).map(ResponseEntity::ok);
  }
}

package com.innowise.authservice.controller;

import com.innowise.authservice.model.dto.AuthRequest;
import com.innowise.authservice.model.dto.AuthResponse;
import com.innowise.authservice.model.dto.RefreshTokenRequest;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final JwtService jwtService;

  @PostMapping("/register")
  public ResponseEntity<Void> register(@RequestBody @Valid AuthRequest request) {
    User user = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
    AuthResponse login = authService.login(request);
    return ResponseEntity.ok(login);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
      AuthResponse refresh = authService.refresh(request.refreshToken());
      return ResponseEntity.ok(refresh);
  }
}

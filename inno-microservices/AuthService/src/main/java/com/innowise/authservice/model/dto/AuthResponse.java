package com.innowise.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthResponse(
    @NotBlank(message = "Access token must not be blank") String accessToken,
    @NotBlank(message = "Refresh token must not be blank") String refreshToken,
    @NotBlank(message = "Type must not be blank") String type) {
  public AuthResponse(String accessToken, String refreshToken) {
    this(accessToken, refreshToken, "Bearer");
  }
}

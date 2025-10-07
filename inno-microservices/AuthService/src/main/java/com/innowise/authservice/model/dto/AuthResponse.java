package com.innowise.authservice.model.dto;

public record AuthResponse(String accessToken, String refreshToken, String type) {
  public AuthResponse(String accessToken, String refreshToken) {
    this(accessToken, refreshToken, "Bearer");
  }
}

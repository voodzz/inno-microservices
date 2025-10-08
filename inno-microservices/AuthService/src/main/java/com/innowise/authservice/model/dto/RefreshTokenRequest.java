package com.innowise.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "refresh token must not be blank") String refreshToken) {}

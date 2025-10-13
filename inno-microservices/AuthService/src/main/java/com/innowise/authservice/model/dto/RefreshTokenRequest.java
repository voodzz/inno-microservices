package com.innowise.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object representing a request to refresh an access token using a refresh token.
 *
 * <p>This record is used as the request body for token refresh endpoints (for example, {@code POST
 * /api/v1/auth/refresh}). The {@code refreshToken} component is validated with {@link NotBlank} and
 * must not be empty or blank.
 *
 * <p>Example JSON:
 *
 * <pre>{@code
 * {
 *   "refreshToken": "d1f4e5a6-7b8c-9d0e-f1a2-34567890abcd"
 * }
 * }</pre>
 *
 * @param refreshToken the refresh token provided by the client; must not be blank
 */
public record RefreshTokenRequest(
    @NotBlank(message = "refresh token must not be blank") String refreshToken) {}

package com.innowise.apigateway.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object representing the authentication response returned to clients after a
 * successful login or token refresh operation.
 *
 * <p>The record contains:
 *
 * <ul>
 *   <li>{@code accessToken} — the short-lived access token to be used in the {@code
 *       Authorization} header on subsequent requests.
 *   <li>{@code refreshToken} — a longer-lived token used to obtain new access tokens.
 *   <li>{@code type} — the token type prefix typically used in the {@code Authorization} header
 *       (for example, {@code "Bearer"}).
 * </ul>
 *
 * <p>All components are validated with {@link NotBlank}; none of the fields may be blank.
 *
 * <p>Convenience constructor {@link #AuthResponse(String, String)} sets the {@code type} to {@code
 * "Bearer"} by default.
 *
 * <p>Example JSON response:
 *
 * <pre>{@code
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "refreshToken": "d1f4e5a6-7b8c-9d0e-f1a2-34567890abcd",
 *   "type": "Bearer"
 * }
 * }</pre>
 *
 * @param accessToken the access token to be used for authorization; must not be blank
 * @param refreshToken the refresh token used to obtain new access tokens; must not be blank
 * @param type the token type/prefix (for example, {@code "Bearer"}); must not be blank
 */
public record AuthResponse(
        @NotBlank(message = "Access token must not be blank") String accessToken,
        @NotBlank(message = "Refresh token must not be blank") String refreshToken,
        @NotBlank(message = "Type must not be blank") String type) {
    public AuthResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }
}

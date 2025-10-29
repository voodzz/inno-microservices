package com.innowise.apigateway.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object representing authentication credentials submitted by clients.
 *
 * <p>This record is used as the request body for authentication endpoints (e.g. {@code /login} and
 * {@code /register}). It is immutable and its components are validated using Jakarta Bean
 * Validation annotations.
 *
 * <p>Validation constraints:
 *
 * <ul>
 *   <li>{@code username} — must not be blank ({@link NotBlank}).
 *   <li>{@code password} — must not be blank ({@link NotBlank}).
 * </ul>
 *
 * <p>Example JSON:
 *
 * <pre>{@code
 * {
 *   "username": "alice",
 *   "password": "s3cr3t"
 * }
 * }</pre>
 *
 * @param username the username supplied by the client; must not be blank
 * @param password the password supplied by the client; must not be blank
 */
public record AuthRequest(
    @NotBlank(message = "Username must not be blank") String username,
    @NotBlank(message = "Password must not be blank") String password) {}

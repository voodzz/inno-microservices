package com.innowise.authservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object (DTO) for new user registration requests. Represents the data required to
 * create a new user account.
 *
 * @param name The user's first name (must not be blank).
 * @param surname The user's last name (must not be blank).
 * @param birthDate The user's date of birth (must be in the past).
 * @param email The user's email address (must be a valid format and not blank).
 * @param password The user's plain text password (must not be blank).
 * @param cards A list of {@link CardDto}s to associate with the user (must not be null).
 */
public record RegistrationRequest(
    @NotBlank String name,
    @NotBlank String surname,
    @Past LocalDate birthDate,
    @Email @NotBlank String email,
    @NotBlank String password,
    @NotNull List<CardDto> cards) {}

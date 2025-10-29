package com.innowise.apigateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object (DTO) for responses from the User Service.
 * Represents the structured data of a user entity.
 *
 * @param id The unique identifier of the user.
 * @param name The user's first name.
 * @param surname The user's last name.
 * @param birthDate The user's date of birth.
 * @param email The user's email address.
 * @param cards A list of {@link CardDto}s associated with the user.
 */
public record UserServiceResponse(
    Long id,
    @NotBlank String name,
    @NotBlank String surname,
    @Past LocalDate birthDate,
    @Email @NotBlank String email,
    List<CardDto> cards) {}

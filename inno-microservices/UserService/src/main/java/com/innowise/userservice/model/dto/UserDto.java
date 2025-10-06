package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object (DTO) representing a user.
 *
 * <p>This record is immutable and used for transferring user data between layers of the
 * application. Validation annotations ensure data integrity.
 *
 * @param id the unique identifier of the user
 * @param name the user's first name (must not be blank)
 * @param surname the user's last name (must not be blank)
 * @param birthDate the user's birthdate (must be in the past)
 * @param email the user's email address (must be a valid email and not blank)
 * @param cards the list of cards owned by the user
 */
public record UserDto(
    Long id,
    @NotBlank(message = "name must have value") String name,
    @NotBlank(message = "surname must have value") String surname,
    @Past(message = "birthDate must be in the past") LocalDate birthDate,
    @Email @NotBlank(message = "email must have value") String email,
    List<CardDto> cards) {}

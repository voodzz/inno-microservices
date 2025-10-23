package com.innowise.authservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record RegisterUserRequest(
    @NotBlank(message = "Name must not be blank") String name,
    @NotBlank(message = "Surname must not e blank") String surname,
    @Past(message = "Birthdate must be in the past") LocalDate birthDate,
    @Email @NotBlank(message = "Email must not be blank") String email,
    @NotBlank(message = "Password must not be blank") String password) {}

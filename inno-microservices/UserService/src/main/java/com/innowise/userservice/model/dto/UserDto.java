package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UserDto(
    @NotBlank(message = "name must have value") String name,
    @NotBlank(message = "surname must have value") String surname,
    @Past(message = "birthDate must be in the past") LocalDate birthDate,
    @Email @NotBlank(message = "email must have value") String email) {}

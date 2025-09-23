package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UserUpdateDTO(
    @NotNull(message = "id must have value") Long id,
    @NotBlank(message = "name must have value") String name,
    @NotBlank(message = "surname must have value") String surname,
    @Past(message = "birthDate must be in the past") LocalDate birthDate) {}

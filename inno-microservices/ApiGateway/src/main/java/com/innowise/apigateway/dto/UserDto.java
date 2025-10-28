package com.innowise.apigateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.List;

public record UserDto(
        @NotBlank String name,
        @NotBlank String surname,
        @Past LocalDate birthDate,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotNull List<CardDto> cards) {}

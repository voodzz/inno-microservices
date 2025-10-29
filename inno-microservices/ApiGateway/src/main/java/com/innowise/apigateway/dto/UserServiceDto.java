package com.innowise.apigateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.List;

public record UserServiceDto(
    Long id,
    @NotBlank String name,
    @NotBlank String surname,
    @Past LocalDate birthDate,
    @Email @NotBlank String email,
    List<CardDto> cards) {}

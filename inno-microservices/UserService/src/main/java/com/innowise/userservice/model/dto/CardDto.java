package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CardDto(
        @NotNull(message = "userId must have value") Long userId,
        @NotBlank(message = "number must have value") String number,
        @NotBlank(message = "holder must have value") String holder,
        @Future(message = "expirationDate must be in the future") LocalDate expirationDate
) {}

package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CardUpdateDTO(
    @NotNull(message = "id must have value") Long id,
    @NotNull(message = "usedId must have value") Long userId,
    @NotBlank(message = "holder must have value") String holder,
    @Future(message = "expirationDate must be in the future") LocalDate expirationDate) {}

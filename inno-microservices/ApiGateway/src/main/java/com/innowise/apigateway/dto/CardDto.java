package com.innowise.apigateway.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing a payment card.
 *
 * <p>This record is immutable and used for transferring card data between layers of the
 * application. Validation annotations ensure data integrity.
 *
 * @param id the unique identifier of the card
 * @param userId the identifier of the user who owns the card (must not be {@code null})
 * @param number the card number (must not be blank)
 * @param holder the cardholder's name (must not be blank)
 * @param expirationDate the card expiration date (must be in the future)
 */
public record CardDto(
    Long id,
    @NotNull(message = "userId must have value") Long userId,
    @NotBlank(message = "number must have value") String number,
    @NotBlank(message = "holder must have value") String holder,
    @Future(message = "expirationDate must be in the future") LocalDate expirationDate) {}

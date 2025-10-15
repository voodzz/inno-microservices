package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.StatusEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing an order
 *
 * <p>This record is immutable and used for transferring order data between layers of the
 * application. Validation annotations ensure data integrity.
 *
 * @param id the unique identifier of the order
 * @param userId the ID of the user who placed the order
 * @param status the current status of the order
 * @param creationDate the date when the order was created
 */
public record OrderDto(
        Long id,
        @NotNull(message = "userId must not be null") Long userId,
        @NotNull(message = "order must have a status") StatusEnum status,
        @Past(message = "creationDate must be in the past") LocalDate creationDate,
        @Email String userEmail) {}

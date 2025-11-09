package com.innowise.paymentservice.model.dto;

import com.innowise.paymentservice.model.StatusEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Data Transfer Object (DTO) representing the immutable data structure for a payment transaction.
 * Used primarily for transferring payment data between service layers and over API boundaries.
 *
 * @param id The unique identifier of the payment. Optional on creation.
 * @param orderId The ID of the associated order. Must not be null.
 * @param userId The ID of the user initiating the payment. Must not be null.
 * @param status The current status of the payment. Must not be null.
 * @param timestamp The creation or update timestamp. Must be a time in the past or the present.
 * @param paymentAmount The monetary value of the payment. Must not be null.
 */
public record PaymentDto(
    String id,
    @NotNull Long orderId,
    @NotNull Long userId,
    @NotNull StatusEnum status,
    @NotNull @PastOrPresent Instant timestamp,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal paymentAmount) {}

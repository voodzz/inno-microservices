package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object (DTO) representing an order item
 *
 * <p>This record is immutable and used for transferring order item data between layers of the
 * application. Validation annotations ensure data integrity.
 *
 * @param id the unique identifier of the order item
 * @param orderId the ID of the order that contains this item
 * @param itemId the ID of the item included in the order
 * @param quantity the quantity of the item in the order
 */
public record OrderItemDto(
    Long id,
    @NotNull(message = "orderId must not be null") Long orderId,
    @NotNull(message = "itemId must not be null") Long itemId,
    @NotNull(message = "quantity must not be null") Integer quantity) {}

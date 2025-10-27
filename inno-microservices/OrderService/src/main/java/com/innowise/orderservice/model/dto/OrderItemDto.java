package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object (DTO) representing a single line item within an order.
 *
 * <p>This record is immutable and is used to link a specific item to an order and define its
 * quantity. The associated validation annotations, such as {@code @NotNull}, ensure data integrity
 * when transferring objects between service layers.
 *
 * @param id the unique identifier for this specific order item entry
 * @param orderId the ID of the parent order this item belongs to (must not be null)
 * @param itemId the ID of the product being ordered (must not be null)
 * @param quantity the number of units of this item included in the order (must not be null)
 */
public record OrderItemDto(
    Long id,
    @NotNull(message = "Order ID must not be null") Long orderId,
    @NotNull(message = "Item ID must not be null") Long itemId,
    @NotNull(message = "Quantity must not be null") Integer quantity) {}

package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) representing an item
 *
 * <p>This record is immutable and used for transferring order data between layers of the
 * application. Validation annotations ensure data integrity.
 *
 * @param id the unique identifier of the item
 * @param name the name of the item
 * @param price the price of the item
 */
public record ItemDto(
    Long id,
    @NotBlank(message = "Item must have a name") String name,
    @NotNull(message = "price must not be null") BigDecimal price) {}

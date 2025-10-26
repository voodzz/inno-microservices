package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemDto(
    Long id,
    @NotBlank(message = "Item must have a name") String name,
    @NotNull(message = "Item must have a price") BigDecimal price) {}

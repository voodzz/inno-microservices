package com.innowise.paymentservice.messaging.event;

import com.innowise.paymentservice.model.dto.PaymentDto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCreatedEvent(
    String paymentId,
    Long orderId,
    Long userId,
    String status,
    Instant timestamp,
    BigDecimal paymentAmount) {
  public PaymentCreatedEvent(PaymentDto dto) {
    this(
        dto.id(),
        dto.orderId(),
        dto.userId(),
        dto.status().toString(),
        dto.timestamp(),
        dto.paymentAmount());
  }
}

package com.innowise.orderservice.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCreatedEvent(
    String paymentId,
    Long orderId,
    Long userId,
    String status,
    Instant timestamp,
    BigDecimal paymentAmount) {}



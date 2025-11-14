package com.innowise.orderservice.messaging;

import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import com.innowise.orderservice.service.impl.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final OrderService orderService;

  @KafkaListener(
      topics = "${kafka.topic.payment.created}",
      groupId = "${spring.kafka.consumer.group-id}")
  public void onPaymentCreated(@Payload PaymentCreatedEvent event) {
    log.debug("Received payment-created event for order {}", event.orderId());
    orderService.handlePaymentEvent(event);
  }
}

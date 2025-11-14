package com.innowise.paymentservice.messaging;

import com.innowise.paymentservice.messaging.event.OrderCreatedEvent;
import com.innowise.paymentservice.messaging.event.PaymentCreatedEvent;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

  private final PaymentService paymentService;
  private final PaymentEventProducer paymentEventProducer;

  @KafkaListener(
      topics = "${kafka.topic.order.created}",
      groupId = "${spring.kafka.consumer.group-id}")
  public void onOrderCreated(@Payload OrderCreatedEvent event) {
    log.debug("Received order-created event for order {}", event.orderId());

    PaymentDto paymentDto = paymentService.createPayment(event);
    PaymentCreatedEvent paymentEvent = new PaymentCreatedEvent(paymentDto);

    paymentEventProducer.publishPaymentCreated(paymentEvent);
  }
}

package com.innowise.paymentservice.messaging;

import com.innowise.paymentservice.messaging.event.OrderCreatedEvent;
import com.innowise.paymentservice.messaging.event.PaymentCreatedEvent;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

  private final PaymentService paymentService;
  private final PaymentEventProducer paymentEventProducer;
  private final DlqProducer dlqProducer;

  @KafkaListener(
      topics = "${kafka.topic.order.created}",
      groupId = "${spring.kafka.consumer.group-id}")
  public void onOrderCreated(
      @Payload OrderCreatedEvent event,
      @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
      @Header(KafkaHeaders.OFFSET) Long offset) {
    log.debug("Received order-created event for order {}", event.orderId());

    try {
      PaymentDto paymentDto = paymentService.createPayment(event);
      PaymentCreatedEvent paymentEvent = new PaymentCreatedEvent(paymentDto);
      paymentEventProducer.publishPaymentCreated(paymentEvent);
    } catch (Exception e) {
      log.error(
          "Processing FAILED for order {} at Partition {}/Offset {}. Error: {}",
          event.orderId(),
          partition,
          offset,
          e.getMessage(),
          e);
      dlqProducer.sendToDlq(event, e);
    }
  }
}

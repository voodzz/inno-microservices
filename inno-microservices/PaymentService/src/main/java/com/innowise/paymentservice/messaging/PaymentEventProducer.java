package com.innowise.paymentservice.messaging;

import com.innowise.paymentservice.config.KafkaTopicProperties;
import com.innowise.paymentservice.messaging.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

  private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;
  private final KafkaTopicProperties topicProperties;

  public void publishPaymentCreated(PaymentCreatedEvent event) {
    kafkaTemplate
        .send(topicProperties.created(), event.orderId().toString(), event)
        .whenComplete(
            (result, throwable) -> {
              if (throwable != null) {
                log.error(
                    "Failed to publish payment-created event for order {}",
                    event.orderId(),
                    throwable);
              } else {
                log.debug(
                    "Published payment-created event for order {} to partition {}",
                    event.orderId(),
                    result.getRecordMetadata().partition());
              }
            });
  }
}

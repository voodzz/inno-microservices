package com.innowise.orderservice.messaging;

import com.innowise.orderservice.config.KafkaTopicProperties;
import com.innowise.orderservice.messaging.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

  private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
  private final KafkaTopicProperties topicProperties;

  public void publishOrderCreated(OrderCreatedEvent event) {
    kafkaTemplate
        .send(topicProperties.orderCreated(), event.orderId().toString(), event)
        .whenComplete(
            (result, throwable) -> {
              if (throwable != null) {
                log.error("Failed to publish order-created event for order {}", event.orderId(), throwable);
              } else {
                log.debug(
                    "Published order-created event for order {} to partition {}",
                    event.orderId(),
                    result.getRecordMetadata().partition());
              }
            });
  }
}



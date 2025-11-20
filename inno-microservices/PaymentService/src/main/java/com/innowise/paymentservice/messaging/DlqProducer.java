package com.innowise.paymentservice.messaging;

import com.innowise.paymentservice.config.KafkaTopicProperties;
import com.innowise.paymentservice.messaging.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqProducer {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  private final KafkaTopicProperties topicProperties;

  public void sendToDlq(OrderCreatedEvent failedEvent, Exception originalException) {
    log.error(
        "Sending failed OrderCreatedEvent to DLQ: Topic={}, OrderId={}, Error={}",
        topicProperties.dlq(),
        failedEvent.orderId(),
        originalException.getMessage(),
        originalException);
    kafkaTemplate.send(topicProperties.dlq(), failedEvent.orderId().toString(), failedEvent);
  }
}

package com.innowise.paymentservice.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

  private final KafkaTopicProperties topicProperties;

  @Bean
  public NewTopic paymentTopic() {
    return TopicBuilder.name(topicProperties.created()).build();
  }

  @Bean
  public NewTopic dlqTopic() {
    return TopicBuilder.name(topicProperties.dlq()).build();
  }
}

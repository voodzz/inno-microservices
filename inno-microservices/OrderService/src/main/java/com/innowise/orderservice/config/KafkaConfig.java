package com.innowise.orderservice.config;

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
  public NewTopic orderTopic() {
    return TopicBuilder.name(topicProperties.created()).build();
  }
}

package com.innowise.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topic.order")
public record KafkaTopicProperties(String created) {}

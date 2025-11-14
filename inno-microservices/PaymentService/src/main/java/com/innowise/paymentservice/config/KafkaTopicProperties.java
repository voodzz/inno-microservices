package com.innowise.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topic.payment")
public record KafkaTopicProperties(String created) {}

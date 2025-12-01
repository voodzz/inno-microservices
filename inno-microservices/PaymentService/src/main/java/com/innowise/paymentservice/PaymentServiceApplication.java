package com.innowise.paymentservice;

import com.innowise.paymentservice.config.ExternalApiProperties;
import com.innowise.paymentservice.config.KafkaTopicProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class})
@EnableConfigurationProperties({ExternalApiProperties.class, KafkaTopicProperties.class})
public class PaymentServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}

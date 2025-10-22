package com.innowise.orderservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Value("${security.jwt.secret-key}")
  private String internalSecretKey;

  @Bean
  public RequestInterceptor internalServiceRequestInterceptor() {
    return requestTemplate -> requestTemplate.header("X-Internal-Secret", internalSecretKey);
  }
}

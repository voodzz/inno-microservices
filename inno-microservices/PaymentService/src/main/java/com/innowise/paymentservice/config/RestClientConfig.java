package com.innowise.paymentservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

  private final ExternalApiProperties externalApiProperties;

  @Bean
  public RestClient restClient() {
    Duration connectTimeout = Duration.ofMillis(externalApiProperties.connectTimeout());
    Duration readTimeout = Duration.ofMillis(externalApiProperties.readTimeout());

    var settings =
        ClientHttpRequestFactorySettings.defaults()
            .withConnectTimeout(connectTimeout)
            .withReadTimeout(readTimeout);

    var requestFactory = ClientHttpRequestFactoryBuilder.detect().build(settings);

    return RestClient.builder().requestFactory(requestFactory).build();
  }
}

package com.innowise.paymentservice.service;

import com.innowise.paymentservice.config.ExternalApiProperties;
import com.innowise.paymentservice.exception.ExternalApiException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RandomNumberService {

  private static final ParameterizedTypeReference<List<Integer>> RESPONSE_TYPE =
      new ParameterizedTypeReference<>() {};

  private final RestClient restClient;
  private final ExternalApiProperties properties;

  @Retry(name = "randomNumberApiRetry", fallbackMethod = "fallback")
  public int fetchRandomNumber() {
    try {
      List<Integer> responseBody =
          restClient.get().uri(properties.url()).retrieve().body(RESPONSE_TYPE);

      if (responseBody == null || responseBody.isEmpty()) {
        throw new ExternalApiException("Failed to retrieve a random number: empty response");
      }

      return responseBody.getFirst();
    } catch (RestClientException ex) {
      throw new ExternalApiException("Failed to retrieve a random number", ex);
    }
  }

  public void fallback(Exception ex) {
    throw new ExternalApiException("All retries have failed for fetching random number: ", ex);
  }
}

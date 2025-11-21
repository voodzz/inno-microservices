package com.innowise.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.innowise.paymentservice.config.ExternalApiProperties;
import com.innowise.paymentservice.exception.ExternalApiException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient.ResponseSpec;

@ExtendWith(MockitoExtension.class)
class RandomNumberServiceTest {

  private static final String API_URL = "http://test-api.com/random";

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
  @Mock private RestClient.RequestHeadersSpec requestHeadersSpec;
  @Mock private ResponseSpec responseSpec;

  private ExternalApiProperties properties;
  private RandomNumberService randomNumberService;

  @BeforeEach
  void setUp() {
    properties = new ExternalApiProperties(API_URL, 5000L, 5000L);
    randomNumberService = new RandomNumberService(restClient, properties);
  }

  @Test
  void fetchRandomNumber_shouldReturnFirstNumberFromResponse() {
    List<Integer> responseBody = List.of(42, 100, 200);
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(API_URL)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(responseBody);

    int result = randomNumberService.fetchRandomNumber();

    assertThat(result).isEqualTo(42);
  }

  @Test
  void fetchRandomNumber_shouldReturnSingleNumberFromResponse() {
    List<Integer> responseBody = List.of(7);
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(API_URL)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(responseBody);

    int result = randomNumberService.fetchRandomNumber();

    assertThat(result).isEqualTo(7);
  }

  @Test
  void fetchRandomNumber_shouldThrowExceptionWhenResponseIsNull() {
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(API_URL)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(null);

    assertThatThrownBy(() -> randomNumberService.fetchRandomNumber())
        .isInstanceOf(ExternalApiException.class)
        .hasMessageContaining("empty response");
  }

  @Test
  void fetchRandomNumber_shouldThrowExceptionWhenResponseIsEmpty() {
    List<Integer> emptyResponse = new ArrayList<>();
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(API_URL)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(emptyResponse);

    assertThatThrownBy(() -> randomNumberService.fetchRandomNumber())
        .isInstanceOf(ExternalApiException.class)
        .hasMessageContaining("empty response");
  }

  @Test
  void fetchRandomNumber_shouldThrowExceptionWhenRestClientThrowsException() {
    RestClientException restClientException = new RestClientException("Connection failed");
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(API_URL)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(any(ParameterizedTypeReference.class)))
        .thenThrow(restClientException);

    assertThatThrownBy(() -> randomNumberService.fetchRandomNumber())
        .isInstanceOf(ExternalApiException.class)
        .hasMessageContaining("Failed to retrieve a random number")
        .hasCause(restClientException);
  }
}



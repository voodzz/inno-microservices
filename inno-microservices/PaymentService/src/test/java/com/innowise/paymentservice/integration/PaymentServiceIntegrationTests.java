package com.innowise.paymentservice.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.paymentservice.exception.ExternalApiException;
import com.innowise.paymentservice.exception.PaymentCreationException;
import com.innowise.paymentservice.messaging.OrderEventListener;
import com.innowise.paymentservice.messaging.PaymentEventProducer;
import com.innowise.paymentservice.messaging.event.OrderCreatedEvent;
import com.innowise.paymentservice.model.StatusEnum;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class PaymentServiceIntegrationTests {

  @MockitoBean private OrderEventListener orderEventListener;

  @MockitoBean private PaymentEventProducer paymentEventProducer;

  @Container
  static MongoDBContainer mongoDBContainer =
      new MongoDBContainer(DockerImageName.parse("mongo:8.2.1")).withReuse(true);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("mongo.url", () -> mongoDBContainer.getReplicaSetUrl("paymentservice"));
    registry.add(
        "spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl("paymentservice"));
    registry.add("external.api.url", () -> "http://localhost:8090/api/v1.0/random");
    registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    registry.add("kafka.topic.order.created", () -> "test-topic");
    registry.add("kafka.topic.payment.created", () -> "test-topic");
  }

  private static WireMockServer wireMockServer;

  @Autowired private PaymentService paymentService;

  @Autowired private PaymentRepository paymentRepository;

  @BeforeAll
  static void initialize() {
    wireMockServer = new WireMockServer(options().port(8090));
    wireMockServer.start();
    configureFor("localhost", 8090);
  }

  @BeforeEach
  void setUp() {
    paymentRepository.deleteAll();
    wireMockServer.resetMappings();
  }

  @AfterEach
  void tearDown() {
    wireMockServer.resetMappings();
  }

  @AfterAll
  static void destroy() {
    if (wireMockServer != null) {
      wireMockServer.stop();
      wireMockServer.shutdown();
    }
  }

  @Test
  void createPayment_WithEvenRandomNumber_ShouldReturnSuccessfulPayment() {
    Long orderId = 100L;
    Long userId = 200L;
    BigDecimal amount = BigDecimal.valueOf(150.50);
    Instant createdAt = Instant.now();

    stubFor(
        get(urlMatching("/api/v1.0/random.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[42]")));

    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);

    PaymentDto result = paymentService.createPayment(event);

    assertThat(result).isNotNull();
    assertThat(result.orderId()).isEqualTo(orderId);
    assertThat(result.userId()).isEqualTo(userId);
    assertThat(result.paymentAmount()).isEqualByComparingTo(amount);
    assertThat(result.status()).isEqualTo(StatusEnum.SUCCESS);
    assertThat(result.timestamp()).isNotNull();
    assertThat(result.id()).isNotNull();

    List<Payment> savedPayments = paymentRepository.findAll();
    assertThat(savedPayments).hasSize(1);
    Payment savedPayment = savedPayments.getFirst();
    assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
    assertThat(savedPayment.getUserId()).isEqualTo(userId);
    assertThat(savedPayment.getPaymentAmount()).isEqualByComparingTo(amount);
    assertThat(savedPayment.getStatus()).isEqualTo(StatusEnum.SUCCESS);
    assertThat(savedPayment.getTimestamp()).isNotNull();
  }

  @Test
  void createPayment_WithOddRandomNumber_ShouldReturnFailedPayment() {
    Long orderId = 101L;
    Long userId = 201L;
    BigDecimal amount = BigDecimal.valueOf(99.99);
    Instant createdAt = Instant.now();

    stubFor(
        get(urlMatching("/api/v1.0/random.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[15]")));

    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);

    PaymentDto result = paymentService.createPayment(event);

    assertThat(result).isNotNull();
    assertThat(result.orderId()).isEqualTo(orderId);
    assertThat(result.userId()).isEqualTo(userId);
    assertThat(result.paymentAmount()).isEqualByComparingTo(amount);
    assertThat(result.status()).isEqualTo(StatusEnum.FAILED);
    assertThat(result.timestamp()).isNotNull();
    assertThat(result.id()).isNotNull();

    List<Payment> savedPayments = paymentRepository.findAll();
    assertThat(savedPayments).hasSize(1);
    Payment savedPayment = savedPayments.getFirst();
    assertThat(savedPayment.getStatus()).isEqualTo(StatusEnum.FAILED);
    assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
  }

  @Test
  void createPayment_WithZeroRandomNumber_ShouldReturnSuccessfulPayment() {
    Long orderId = 102L;
    Long userId = 202L;
    BigDecimal amount = BigDecimal.valueOf(250.00);
    Instant createdAt = Instant.now();

    stubFor(
        get(urlMatching("/api/v1.0/random.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[0]")));

    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);

    PaymentDto result = paymentService.createPayment(event);

    assertThat(result.status()).isEqualTo(StatusEnum.SUCCESS);
    assertThat(result.orderId()).isEqualTo(orderId);
    assertThat(result.paymentAmount()).isEqualByComparingTo(amount);
  }

  @Test
  void createPayment_WithNegativeEvenRandomNumber_ShouldReturnSuccessfulPayment() {
    Long orderId = 103L;
    Long userId = 203L;
    BigDecimal amount = BigDecimal.valueOf(75.50);
    Instant createdAt = Instant.now();

    stubFor(
        get(urlMatching("/api/v1.0/random.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[-4]")));

    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);

    PaymentDto result = paymentService.createPayment(event);

    assertThat(result.status()).isEqualTo(StatusEnum.SUCCESS);
    assertThat(result.orderId()).isEqualTo(orderId);
  }

  @Test
  void createPayment_WithNegativeOddRandomNumber_ShouldReturnFailedPayment() {
    Long orderId = 104L;
    Long userId = 204L;
    BigDecimal amount = BigDecimal.valueOf(125.00);
    Instant createdAt = Instant.now();

    stubFor(
        get(urlMatching("/api/v1.0/random.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[-5]")));

    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);

    PaymentDto result = paymentService.createPayment(event);

    assertThat(result.status()).isEqualTo(StatusEnum.FAILED);
    assertThat(result.orderId()).isEqualTo(orderId);
  }

  @Test
  void createPayment_WhenExternalApiReturnsError_ShouldThrowException() {
    Long orderId = 105L;
    Long userId = 205L;
    BigDecimal amount = BigDecimal.valueOf(50.00);
    Instant createdAt = Instant.now();

    stubFor(
        get(urlMatching("/api/v1.0/random.*"))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("Internal Server Error")));

    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);

    assertThatThrownBy(() -> paymentService.createPayment(event))
        .isInstanceOf(PaymentCreationException.class);

    List<Payment> savedPayments = paymentRepository.findAll();
    assertThat(savedPayments).isEmpty();
  }

  @Test
  void createPayment_WhenExternalApiReturnsEmptyResponse_ShouldThrowException() {
    Long orderId = 106L;
    Long userId = 206L;
    BigDecimal amount = BigDecimal.valueOf(75.00);
    Instant createdAt = Instant.now();

    stubFor(
        get(urlMatching("/api/v1.0/random.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[]")));

    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);

    assertThatThrownBy(() -> paymentService.createPayment(event))
        .isInstanceOf(PaymentCreationException.class);

    List<Payment> savedPayments = paymentRepository.findAll();
    assertThat(savedPayments).isEmpty();
  }

  @Test
  void createPayment_ShouldHandleMultiplePayments() {
    stubFor(
        get(urlMatching("/api/v1.0/random.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[10]")));

    OrderCreatedEvent event1 =
        new OrderCreatedEvent(107L, 207L, BigDecimal.valueOf(100.00), Instant.now());
    OrderCreatedEvent event2 =
        new OrderCreatedEvent(108L, 208L, BigDecimal.valueOf(200.00), Instant.now());
    OrderCreatedEvent event3 =
        new OrderCreatedEvent(109L, 209L, BigDecimal.valueOf(300.00), Instant.now());

    PaymentDto result1 = paymentService.createPayment(event1);
    PaymentDto result2 = paymentService.createPayment(event2);
    PaymentDto result3 = paymentService.createPayment(event3);

    assertThat(result1.orderId()).isEqualTo(107L);
    assertThat(result2.orderId()).isEqualTo(108L);
    assertThat(result3.orderId()).isEqualTo(109L);

    List<Payment> savedPayments = paymentRepository.findAll();
    assertThat(savedPayments).hasSize(3);
    assertThat(savedPayments)
        .extracting(Payment::getOrderId)
        .containsExactlyInAnyOrder(107L, 108L, 109L);
  }

  @Test
  void createPayment_ShouldSetCorrectTimestamp() {
    Long orderId = 110L;
    Long userId = 210L;
    BigDecimal amount = BigDecimal.valueOf(50.00);
    Instant beforeCreation = Instant.now();

    stubFor(
        get(urlMatching("/api/v1.0/random.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[8]")));

    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, Instant.now());

    PaymentDto result = paymentService.createPayment(event);

    Instant afterCreation = Instant.now();

    assertThat(result.timestamp()).isNotNull();
    assertThat(result.timestamp()).isAfterOrEqualTo(beforeCreation);
    assertThat(result.timestamp()).isBeforeOrEqualTo(afterCreation);

    Payment savedPayment = paymentRepository.findById(result.id()).orElseThrow();
    assertThat(savedPayment.getTimestamp()).isNotNull();
    assertThat(savedPayment.getTimestamp())
        .isAfterOrEqualTo(result.timestamp().minusSeconds(1))
        .isBeforeOrEqualTo(result.timestamp().plusSeconds(1));
  }
}

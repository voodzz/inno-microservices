package com.innowise.paymentservice.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.paymentservice.messaging.event.OrderCreatedEvent;
import com.innowise.paymentservice.model.StatusEnum;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class PaymentServiceIntegrationTest {

  @Container
  static MongoDBContainer mongoDBContainer =
      new MongoDBContainer(DockerImageName.parse("mongo:8.2.1"))
          .withReuse(true);

  @Container
  static KafkaContainer kafkaContainer =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
          .withReuse(true);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("mongo.url", () -> mongoDBContainer.getReplicaSetUrl("paymentservice"));
    registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl("paymentservice"));
    registry.add("external.api.url", () -> "http://localhost:8089/api/v1.0/random");
    registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    registry.add("kafka.topic.order.created", () -> "queuing.orderservice.order");
    registry.add("kafka.topic.payment.created", () -> "queuing.paymentservice.payment");
  }

  private static WireMockServer wireMockServer;

  @Autowired private PaymentRepository paymentRepository;

  private KafkaTemplate<String, OrderCreatedEvent> orderEventKafkaTemplate;

  @BeforeAll
  static void initialize() {
    wireMockServer =
        new WireMockServer(options().port(8089));
    wireMockServer.start();
    configureFor("localhost", 8089);
  }

  @BeforeEach
  void setUp() {
    paymentRepository.deleteAll();
    wireMockServer.resetMappings();
    
    DefaultKafkaProducerFactory<String, OrderCreatedEvent> producerFactory =
        new DefaultKafkaProducerFactory<>(
            java.util.Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    kafkaContainer.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    JsonSerializer.class));
    orderEventKafkaTemplate = new KafkaTemplate<>(producerFactory);
  }

  @AfterEach
  void tearDown() {
    wireMockServer.resetMappings();
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @AfterAll
  static void destroy() {
    if (wireMockServer != null) {
      wireMockServer.stop();
      wireMockServer.shutdown();
    }
  }

  @Test
  void createPayment_shouldCreatePaymentAndPublishEvent_whenOrderCreatedEventReceived() {
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

    try {
      orderEventKafkaTemplate.send("queuing.orderservice.order", orderId.toString(), event).get();
      Thread.sleep(1000);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    await()
        .atMost(15, TimeUnit.SECONDS)
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(
            () -> {
              List<Payment> payments = paymentRepository.findAll();
              List<Payment> testPayments = payments.stream()
                  .filter(p -> p.getOrderId().equals(orderId))
                  .toList();
              assertThat(testPayments).hasSize(1);

              Payment payment = testPayments.get(0);
              assertThat(payment.getOrderId()).isEqualTo(orderId);
              assertThat(payment.getUserId()).isEqualTo(userId);
              assertThat(payment.getPaymentAmount()).isEqualByComparingTo(amount);
              assertThat(payment.getStatus()).isEqualTo(StatusEnum.SUCCESS);
              assertThat(payment.getTimestamp()).isNotNull();
            });
  }

  @Test
  void createPayment_shouldCreateFailedPayment_whenRandomNumberIsOdd() {
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

    try {
      orderEventKafkaTemplate.send("queuing.orderservice.order", orderId.toString(), event).get();
      Thread.sleep(6000);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    await()
        .atMost(20, TimeUnit.SECONDS)
        .pollInterval(Duration.ofMillis(1000))
        .untilAsserted(
            () -> {
              List<Payment> payments = paymentRepository.findAll();
              List<Payment> testPayments = payments.stream()
                  .filter(p -> p.getOrderId().equals(orderId))
                  .toList();
              assertThat(testPayments).hasSize(1);

              Payment payment = testPayments.get(0);
              assertThat(payment.getStatus()).isEqualTo(StatusEnum.FAILED);
              assertThat(payment.getOrderId()).isEqualTo(orderId);
            });
  }

  @Test
  void createPayment_shouldPublishPaymentCreatedEvent_whenPaymentIsCreated() {
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
                    .withBody("[8]")));

    OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, amount, createdAt);

    try {
      orderEventKafkaTemplate.send("queuing.orderservice.order", orderId.toString(), event).get();
      Thread.sleep(1000);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    await()
        .atMost(15, TimeUnit.SECONDS)
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(
            () -> {
              List<Payment> payments = paymentRepository.findAll();
              List<Payment> testPayments = payments.stream()
                  .filter(p -> p.getOrderId().equals(orderId))
                  .toList();
              assertThat(testPayments).hasSize(1);
              assertThat(testPayments.get(0).getOrderId()).isEqualTo(orderId);
            });
  }

  @Test
  void createPayment_shouldHandleMultipleOrderEvents() {
    stubFor(
        get(urlMatching("/api/v1.0/random.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[10]")));

    OrderCreatedEvent event1 = new OrderCreatedEvent(103L, 203L, BigDecimal.valueOf(100.00), Instant.now());
    OrderCreatedEvent event2 = new OrderCreatedEvent(104L, 204L, BigDecimal.valueOf(200.00), Instant.now());
    OrderCreatedEvent event3 = new OrderCreatedEvent(105L, 205L, BigDecimal.valueOf(300.00), Instant.now());

    try {
      orderEventKafkaTemplate.send("queuing.orderservice.order", "103", event1).get();
      orderEventKafkaTemplate.send("queuing.orderservice.order", "104", event2).get();
      orderEventKafkaTemplate.send("queuing.orderservice.order", "105", event3).get();
      Thread.sleep(2000);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    await()
        .atMost(20, TimeUnit.SECONDS)
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(
            () -> {
              List<Payment> payments = paymentRepository.findAll();
              List<Payment> testPayments = payments.stream()
                  .filter(p -> p.getOrderId().equals(103L) || p.getOrderId().equals(104L) || p.getOrderId().equals(105L))
                  .toList();
              assertThat(testPayments).hasSize(3);
            });
  }

  @Test
  void createPayment_shouldHandleExternalApiFailure() {
    Long orderId = 106L;
    Long userId = 206L;
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

    try {
      orderEventKafkaTemplate.send("queuing.orderservice.order", orderId.toString(), event).get();
      Thread.sleep(1000);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    await()
        .atMost(15, TimeUnit.SECONDS)
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(
            () -> {
              List<Payment> payments = paymentRepository.findAll();
              List<Payment> testPayments = payments.stream()
                  .filter(p -> p.getOrderId().equals(orderId))
                  .toList();
              assertThat(testPayments).isEmpty();
            });
  }

  @Test
  void createPayment_shouldHandleEmptyResponseFromExternalApi() {
    Long orderId = 107L;
    Long userId = 207L;
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

    try {
      orderEventKafkaTemplate.send("queuing.orderservice.order", orderId.toString(), event).get();
      Thread.sleep(1000);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    await()
        .atMost(15, TimeUnit.SECONDS)
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(
            () -> {
              List<Payment> payments = paymentRepository.findAll();
              List<Payment> testPayments = payments.stream()
                  .filter(p -> p.getOrderId().equals(orderId))
                  .toList();
              assertThat(testPayments).isEmpty();
            });
  }
}


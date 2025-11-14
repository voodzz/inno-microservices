package com.innowise.orderservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.innowise.orderservice.messaging.OrderEventProducer;
import com.innowise.orderservice.messaging.event.OrderCreatedEvent;
import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import com.innowise.orderservice.model.StatusEnum;
import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.dto.UserDto;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.impl.OrderService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OrderServiceKafkaIntegrationTests {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:18")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @Container
  static KafkaContainer kafkaContainer =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0")).withReuse(true);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("userservice.url", () -> "http://localhost:8090");
    registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    registry.add("spring.kafka.consumer.group-id", () -> "order-service-test");
    registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    registry.add(
        "spring.kafka.consumer.key-deserializer",
        () -> "org.apache.kafka.common.serialization.StringDeserializer");
    registry.add(
        "spring.kafka.consumer.value-deserializer",
        () -> "org.springframework.kafka.support.serializer.JsonDeserializer");
    registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages", () -> "*");
    registry.add(
        "spring.kafka.consumer.properties.spring.json.value.default.type",
        () -> "com.innowise.orderservice.messaging.event.PaymentCreatedEvent");
    registry.add("spring.kafka.consumer.properties.spring.json.use.type.headers", () -> "false");
    registry.add(
        "spring.kafka.producer.key-serializer",
        () -> "org.apache.kafka.common.serialization.StringSerializer");
    registry.add(
        "spring.kafka.producer.value-serializer",
        () -> "org.springframework.kafka.support.serializer.JsonSerializer");
    registry.add("spring.kafka.producer.properties.spring.json.add.type.headers", () -> "false");
    registry.add("kafka.topic.order.created", () -> "queuing.orderservice.order");
    registry.add("kafka.topic.payment.created", () -> "queuing.paymentservice.payment");
  }

  private static WireMockServer wireMockServer;

  @Autowired private OrderService orderService;

  @Autowired private OrderRepository orderRepository;

  @Autowired private ObjectMapper objectMapper;

  @MockitoSpyBean private OrderEventProducer orderEventProducer;

  private Consumer<String, OrderCreatedEvent> orderEventConsumer;
  private KafkaTemplate<String, PaymentCreatedEvent> paymentEventKafkaTemplate;

  private UserDto testUser;
  private OrderDto testOrderDto;

  @BeforeAll
  static void initialize() {
    wireMockServer =
        new WireMockServer(
            com.github.tomakehurst.wiremock.core.WireMockConfiguration.options()
                .port(8090)
                .extensions(new ResponseTemplateTransformer(false)));
    wireMockServer.start();
    com.github.tomakehurst.wiremock.client.WireMock.configureFor("localhost", 8090);
  }

  @BeforeEach
  void setUp() {
    testUser = new UserDto(1L, "John", "Doe", LocalDate.of(1990, 1, 1), "john.doe@example.com");
    testOrderDto =
        new OrderDto(
            null,
            1L,
            StatusEnum.PENDING,
            LocalDate.now().minusDays(1),
            List.of(),
            "john.doe@example.com");

    orderRepository.deleteAll();
    wireMockServer.resetMappings();

    DefaultKafkaConsumerFactory<String, OrderCreatedEvent> consumerFactory =
        new DefaultKafkaConsumerFactory<>(
            java.util.Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-order-consumer-" + System.currentTimeMillis(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                "true",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES,
                "*",
                JsonDeserializer.VALUE_DEFAULT_TYPE,
                OrderCreatedEvent.class.getName(),
                JsonDeserializer.USE_TYPE_INFO_HEADERS,
                false));
    orderEventConsumer = consumerFactory.createConsumer();
    orderEventConsumer.subscribe(List.of("queuing.orderservice.order"));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .pollInterval(Duration.ofMillis(100))
        .until(
            () -> {
              orderEventConsumer.poll(Duration.ofMillis(100));
              var assignment = orderEventConsumer.assignment();
              if (!assignment.isEmpty()) {
                orderEventConsumer.seekToEnd(assignment);
                return true;
              }
              return false;
            });

    DefaultKafkaProducerFactory<String, PaymentCreatedEvent> producerFactory =
        new DefaultKafkaProducerFactory<>(
            java.util.Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class));
    paymentEventKafkaTemplate = new KafkaTemplate<>(producerFactory);
  }

  @AfterEach
  void tearDown() {
    wireMockServer.resetMappings();
    if (orderEventConsumer != null) {
      orderEventConsumer.close();
    }
    try {
      Thread.sleep(1000);
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
  @Transactional
  void createOrder_ShouldPublishOrderCreatedEvent() throws Exception {
    setupUserServiceMock(testUser, 200);

    var result = orderService.create(testOrderDto);

    assertThat(result.orderDto().id()).isNotNull();
    assertThat(result.orderDto().status()).isEqualTo(StatusEnum.PENDING);

    var captor = org.mockito.ArgumentCaptor.forClass(OrderCreatedEvent.class);
    org.mockito.Mockito.verify(orderEventProducer, org.mockito.Mockito.timeout(5_000))
        .publishOrderCreated(captor.capture());

    OrderCreatedEvent event = captor.getValue();
    assertThat(event).isNotNull();
    assertThat(event.orderId()).isEqualTo(result.orderDto().id());
    assertThat(event.userId()).isEqualTo(testUser.id());
    assertThat(event.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(event.createdAt()).isNotNull();
  }

  @Test
  void handlePaymentEvent_ShouldUpdateOrderStatusToConfirmed_WhenPaymentIsSuccessful()
      throws Exception {
    setupUserServiceMock(testUser, 200);

    var orderResult = orderService.create(testOrderDto);
    Long orderId = orderResult.orderDto().id();

    Thread.sleep(100);

    Order order = orderRepository.findById(orderId).orElseThrow();
    assertThat(order.getStatus()).isEqualTo(StatusEnum.PENDING);

    PaymentCreatedEvent paymentEvent =
        new PaymentCreatedEvent(
            "payment-123",
            orderId,
            testUser.id(),
            "SUCCESS",
            Instant.now(),
            BigDecimal.valueOf(100.00));

    try {
      paymentEventKafkaTemplate
          .send("queuing.paymentservice.payment", orderId.toString(), paymentEvent)
          .get(5, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send payment event", e);
    }

    await()
        .atMost(15, TimeUnit.SECONDS)
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(
            () -> {
              Order updatedOrder =
                  orderRepository
                      .findById(orderId)
                      .orElseThrow(() -> new AssertionError("Order not found: " + orderId));
              assertThat(updatedOrder.getStatus()).isEqualTo(StatusEnum.CONFIRMED);
            });
  }

  @Test
  void handlePaymentEvent_ShouldUpdateOrderStatusToPaymentFailed_WhenPaymentIsFailed()
      throws Exception {
    setupUserServiceMock(testUser, 200);

    var orderResult = orderService.create(testOrderDto);
    Long orderId = orderResult.orderDto().id();

    Thread.sleep(100);

    Order order = orderRepository.findById(orderId).orElseThrow();
    assertThat(order.getStatus()).isEqualTo(StatusEnum.PENDING);

    PaymentCreatedEvent paymentEvent =
        new PaymentCreatedEvent(
            "payment-456",
            orderId,
            testUser.id(),
            "FAILED",
            Instant.now(),
            BigDecimal.valueOf(100.00));

    try {
      paymentEventKafkaTemplate
          .send("queuing.paymentservice.payment", orderId.toString(), paymentEvent)
          .get(5, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send payment event", e);
    }

    await()
        .atMost(15, TimeUnit.SECONDS)
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(
            () -> {
              Order updatedOrder =
                  orderRepository
                      .findById(orderId)
                      .orElseThrow(() -> new AssertionError("Order not found: " + orderId));
              assertThat(updatedOrder.getStatus()).isEqualTo(StatusEnum.PAYMENT_FAILED);
            });
  }

  @Test
  void handlePaymentEvent_ShouldNotUpdateOrder_WhenOrderDoesNotExist() throws Exception {
    Long nonExistentOrderId = 999L;

    PaymentCreatedEvent paymentEvent =
        new PaymentCreatedEvent(
            "payment-789",
            nonExistentOrderId,
            testUser.id(),
            "SUCCESS",
            Instant.now(),
            BigDecimal.valueOf(100.00));

    try {
      paymentEventKafkaTemplate
          .send("queuing.paymentservice.payment", nonExistentOrderId.toString(), paymentEvent)
          .get(5, TimeUnit.SECONDS);
      Thread.sleep(1000);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send payment event", e);
    }

    assertThat(orderRepository.findById(nonExistentOrderId)).isEmpty();
  }

  @Test
  @Transactional
  void createOrder_ShouldPublishEventWithCorrectTotalAmount() throws Exception {
    setupUserServiceMock(testUser, 200);

    var result = orderService.create(testOrderDto);

    var captor = org.mockito.ArgumentCaptor.forClass(OrderCreatedEvent.class);
    org.mockito.Mockito.verify(orderEventProducer, org.mockito.Mockito.timeout(5_000))
        .publishOrderCreated(captor.capture());

    OrderCreatedEvent event = captor.getValue();
    assertThat(event.orderId()).isEqualTo(result.orderDto().id());
    assertThat(event.totalAmount()).isNotNull();
  }

  private void setupUserServiceMock(UserDto user, int statusCode) throws Exception {
    if (statusCode == 200 && user != null) {
      com.github.tomakehurst.wiremock.client.WireMock.stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.get(
                  com.github.tomakehurst.wiremock.client.WireMock.urlMatching(
                      "/api/v1/users\\?filter=email&email=.+"))
              .willReturn(
                  com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(objectMapper.writeValueAsString(List.of(user)))));
    } else if (statusCode == 404) {
      com.github.tomakehurst.wiremock.client.WireMock.stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.get(
                  com.github.tomakehurst.wiremock.client.WireMock.urlMatching(
                      "/api/v1/users\\?filter=email&email=.+"))
              .willReturn(
                  com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                      .withStatus(404)
                      .withHeader("Content-Type", "application/json")
                      .withBody("{\"error\":\"User not found\"}")));
    } else if (statusCode == 500) {
      com.github.tomakehurst.wiremock.client.WireMock.stubFor(
          com.github.tomakehurst.wiremock.client.WireMock.get(
                  com.github.tomakehurst.wiremock.client.WireMock.urlMatching(
                      "/api/v1/users\\?filter=email&email=.+"))
              .willReturn(
                  com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                      .withStatus(500)
                      .withHeader("Content-Type", "application/json")
                      .withBody("{\"error\":\"Internal server error\"}")));
    }
  }
}

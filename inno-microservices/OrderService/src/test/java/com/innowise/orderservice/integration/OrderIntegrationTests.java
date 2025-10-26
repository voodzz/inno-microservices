package com.innowise.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.StatusEnum;
import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.dto.UserDto;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.impl.OrderService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@Transactional
public class OrderIntegrationTests {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:18")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("userservice.url", () -> "http://localhost:8089");
  }

  private static WireMockServer wireMockServer;

  @Autowired private OrderService orderService;

  @Autowired private OrderRepository orderRepository;

  @Autowired private OrderMapper orderMapper;

  @Autowired private ObjectMapper objectMapper;

  private UserDto testUser;
  private OrderDto testOrderDto;

  @BeforeAll
  static void initialize() {
    wireMockServer =
        new WireMockServer(options().port(8089).extensions(new ResponseTemplateTransformer(false)));
    configureFor("localhost", 8089);
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
    wireMockServer.start();
  }

  @AfterEach
  void afterEach() {
    wireMockServer.stop();
  }

  @AfterAll
  static void destroy() {
    wireMockServer.shutdown();
  }

  @Test
  void createOrder_WithValidUser_ShouldReturnOrderWithUserData() throws Exception {
    setupUserServiceMock("john.doe@example.com", testUser, 200);

    var result = orderService.create(testOrderDto);

    assertThat(result.orderDto().userId()).isEqualTo(1L);
    assertThat(result.orderDto().status()).isEqualTo(StatusEnum.PENDING);
    assertThat(result.orderDto().userEmail()).isEqualTo("john.doe@example.com");
    assertThat(result.userDto().id()).isEqualTo(1L);
    assertThat(result.userDto().name()).isEqualTo("John");
    assertThat(result.userDto().surname()).isEqualTo("Doe");
    assertThat(result.userDto().email()).isEqualTo("john.doe@example.com");

    List<Order> savedOrders = orderRepository.findAll();
    assertThat(savedOrders).hasSize(1);
    assertThat(savedOrders.getFirst().getUserId()).isEqualTo(1L);
    assertThat(savedOrders.getFirst().getStatus()).isEqualTo(StatusEnum.PENDING);
  }

  @Test
  void createOrder_WithNonExistentUser_ShouldThrowException() throws Exception {
    setupUserServiceMock("nonexistent@example.com", null, 404);

    OrderDto orderWithNonExistentUser =
        new OrderDto(
            null,
            1L,
            StatusEnum.PENDING,
            LocalDate.now().minusDays(1),
            List.of(),
            "nonexistent@example.com");

    assertThat(
        assertThrows(
            com.innowise.orderservice.exception.RetrieveUserException.class,
            () -> orderService.create(orderWithNonExistentUser)));

    assertThat(orderRepository.findAll()).isEmpty();
  }

  @Test
  void createOrder_WhenUserServiceIsUnavailable_ShouldThrowException() throws Exception {
    setupUserServiceMock("john.doe@example.com", null, 500);

    assertThrows(
        com.innowise.orderservice.exception.RetrieveUserException.class,
        () -> orderService.create(testOrderDto));

    assertThat(orderRepository.findAll()).isEmpty();
  }

  @Test
  void findById_WithExistingOrder_ShouldReturnOrderWithUserData() throws Exception {
    Order savedOrder = orderRepository.save(orderMapper.toEntity(testOrderDto));
    setupUserServiceMock("john.doe@example.com", testUser, 200);

    var result = orderService.findById(savedOrder.getId());

    assertThat(result.orderDto().id()).isEqualTo(savedOrder.getId());
    assertThat(result.orderDto().userId()).isEqualTo(1L);
    assertThat(result.orderDto().status()).isEqualTo(StatusEnum.PENDING);
    assertThat(result.userDto().name()).isEqualTo("John");
    assertThat(result.userDto().email()).isEqualTo("john.doe@example.com");
  }

  @Test
  void updateById_WithValidData_ShouldUpdateOrder() throws Exception {
    Order savedOrder = orderRepository.save(orderMapper.toEntity(testOrderDto));
    OrderDto updateDto =
        new OrderDto(
            savedOrder.getId(),
            1L,
            StatusEnum.CONFIRMED,
            savedOrder.getCreationDate(),
            List.of(),
            "john.doe@example.com");
    setupUserServiceMock("john.doe@example.com", testUser, 200);

    var result = orderService.updateById(savedOrder.getId(), updateDto);

    assertThat(result.orderDto().status()).isEqualTo(StatusEnum.CONFIRMED);

    Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
    assertThat(updatedOrder.getStatus()).isEqualTo(StatusEnum.CONFIRMED);
  }

  @Test
  void findAll_WithValidData_ShouldReturnList() throws Exception {
    var testUserFriend =
        new UserDto(2L, "Jane", "Doe", LocalDate.of(1991, 2, 4), "jane.doe@example.com");
    setupUserServiceMock("jane.doe@example.com", testUserFriend, 200);
    setupUserServiceMock("john.doe@example.com", testUser, 200);

    var anotherTestOrder =
        new Order(
            null,
            testUserFriend.id(),
            StatusEnum.CONFIRMED,
            LocalDate.now().minusDays(1),
            testUserFriend.email(),
            null);

    orderRepository.saveAll(List.of(orderMapper.toEntity(testOrderDto), anotherTestOrder));

    var list = orderRepository.findAll();

    assertThat(list).isNotNull();
    assertThat(list).hasSize(2);
    assertThat(list.getFirst()).isNotNull();
  }

  private void setupUserServiceMock(String email, UserDto user, int statusCode) throws Exception {
    if (statusCode == 200 && user != null) {
      stubFor(
          get(urlPathEqualTo("/api/v1/users"))
              .withQueryParam("email", equalTo(email))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(objectMapper.writeValueAsString(user))));
    } else if (statusCode == 404) {
      stubFor(
          get(urlPathEqualTo("/api/v1/users"))
              .withQueryParam("email", equalTo(email))
              .willReturn(
                  aResponse()
                      .withStatus(404)
                      .withHeader("Content-Type", "application/json")
                      .withBody("{\"error\":\"User not found\"}")));
    } else if (statusCode == 500) {
      stubFor(
          get(urlPathEqualTo("/api/v1/users"))
              .withQueryParam("email", equalTo(email))
              .willReturn(
                  aResponse()
                      .withStatus(500)
                      .withHeader("Content-Type", "application/json")
                      .withBody("{\"error\":\"Internal server error\"}")));
    }
  }
}

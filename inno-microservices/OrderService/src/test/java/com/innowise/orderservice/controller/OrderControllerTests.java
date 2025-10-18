package com.innowise.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.model.StatusEnum;
import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.dto.OrderUserDto;
import com.innowise.orderservice.model.dto.UserDto;
import com.innowise.orderservice.service.impl.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.argThat;

@WebMvcTest(OrderController.class)
class OrderControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private OrderService orderService;

  private OrderDto testOrderDto;
  private OrderUserDto testOrderUserDto;
  private UserDto testUserDto;

  @BeforeEach
  void setUp() {
    testUserDto = new UserDto(1L, "Ivan", "Ivanov", LocalDate.of(1990, 1, 1), "ivan@test.com");
    testOrderDto =
        new OrderDto(1L, 10L, StatusEnum.PENDING, LocalDate.now().minusDays(1), "ivan@test.com");
    testOrderUserDto = new OrderUserDto(testOrderDto, testUserDto);
  }

  @Test
  void create_ShouldReturnCreatedOrder() throws Exception {
    when(orderService.create(any(OrderDto.class))).thenReturn(testOrderUserDto);

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderDto.id").value(testOrderDto.id()))
        .andExpect(jsonPath("$.userDto.name").value(testUserDto.name()));

    verify(orderService, times(1)).create(any(OrderDto.class));
  }

  @Test
  void findById_ShouldReturnOrder() throws Exception {
    Long orderId = 1L;
    when(orderService.findById(orderId)).thenReturn(testOrderUserDto);

    mockMvc
        .perform(get("/api/v1/orders/{id}", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderDto.id").value(orderId))
        .andExpect(jsonPath("$.userDto.email").value(testUserDto.email()));

    verify(orderService, times(1)).findById(orderId);
  }

  @Test
  void update_ShouldReturnNoContent() throws Exception {
    Long orderId = 1L;
    when(orderService.updateById(eq(orderId), any(OrderDto.class))).thenReturn(testOrderUserDto);

    mockMvc
        .perform(
            put("/api/v1/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderDto)))
        .andExpect(status().isNoContent());

    verify(orderService, times(1)).updateById(eq(orderId), any(OrderDto.class));
  }

  @Test
  void delete_ShouldReturnNoContent() throws Exception {
    Long orderId = 1L;
    doNothing().when(orderService).deleteById(orderId);

    mockMvc.perform(delete("/api/v1/orders/{id}", orderId)).andExpect(status().isNoContent());

    verify(orderService, times(1)).deleteById(orderId);
  }

  @Test
  void find_NoParams_ShouldCallFindAll() throws Exception {
    Pageable pageable = PageRequest.of(0, 20);
    Page<OrderUserDto> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

    when(orderService.findBySpecification(any(Specification.class), eq(pageable)))
        .thenReturn(emptyPage);

    ResultActions result =
        mockMvc.perform(get("/api/v1/orders").param("page", "0").param("size", "20"));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").value(0));

    verify(orderService, times(1)).findBySpecification(any(Specification.class), eq(pageable));
  }

  @Test
  void find_WithIdsAndStatuses_ShouldCallAnyOfSpecification() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);

    Page<OrderUserDto> testPage = new PageImpl<>(List.of(testOrderUserDto), pageable, 1);

    when(orderService.findBySpecification(any(Specification.class), eq(pageable)))
        .thenReturn(testPage);

    mockMvc
        .perform(
            get("/api/v1/orders").param("ids", "1", "2").param("statuses", "PENDING", "CONFIRMED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].orderDto.id").value(testOrderDto.id()));

    verify(orderService, times(1)).findBySpecification(any(Specification.class), eq(pageable));
  }

  @Test
  void find_OnlyWithIds_ShouldCallServiceWithIdSpecification() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);

    Page<OrderUserDto> testPage = new PageImpl<>(List.of(testOrderUserDto), pageable, 1);

    when(orderService.findBySpecification(any(Specification.class), eq(pageable)))
        .thenReturn(testPage);

    mockMvc.perform(get("/api/v1/orders").param("ids", "1", "2", "3")).andExpect(status().isOk());

    verify(orderService, times(1)).findBySpecification(any(Specification.class), eq(pageable));

    verify(orderService).findBySpecification(argThat(Objects::nonNull), eq(pageable));
  }

  @Test
  void find_OnlyWithStatuses_ShouldCallServiceWithStatusesSpecification() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);

    Page<OrderUserDto> testPage = new PageImpl<>(List.of(testOrderUserDto), pageable, 1);

    when(orderService.findBySpecification(any(Specification.class), eq(pageable)))
        .thenReturn(testPage);

    mockMvc
        .perform(get("/api/v1/orders").param("statuses", "SHIPPED", "DELIVERED"))
        .andExpect(status().isOk());

    verify(orderService, times(1)).findBySpecification(any(Specification.class), eq(pageable));

    verify(orderService).findBySpecification(argThat(Objects::nonNull), eq(pageable));
  }
}

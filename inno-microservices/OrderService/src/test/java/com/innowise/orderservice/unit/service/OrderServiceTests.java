package com.innowise.orderservice.unit.service;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.exception.RetrieveUserException;
import com.innowise.orderservice.exception.UpdateException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.StatusEnum;
import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.dto.OrderUserDto;
import com.innowise.orderservice.model.dto.UserDto;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.impl.OrderService;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderMapper orderMapper;
  @Mock private UserServiceClient userServiceClient;

  @InjectMocks private OrderService orderService;

  private final Order mockOrderEntity = new Order();
  private final OrderDto mockOrderDto =
      new OrderDto(1L, 100L, StatusEnum.PENDING, LocalDate.now(), "test@example.com");
  private final UserDto mockUserDto = new UserDto(100L, "John", "Doe", null, "test@example.com");

  @Test
  void findById_ShouldReturnOrderUserDto_WhenOrderIsFound() {

    Long orderId = 1L;

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(mockOrderDto.userEmail())).thenReturn(mockUserDto);

    OrderUserDto result = orderService.findById(orderId);

    assertThat(result.orderDto().id()).isEqualTo(orderId);
    verify(orderRepository).findById(orderId);
  }

  @Test
  void findById_ShouldThrowNotFoundException_WhenOrderIsNotFound() {
    Long orderId = 99L;

    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.findById(orderId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(String.valueOf(orderId));

    verify(orderRepository).findById(orderId);
    verifyNoInteractions(userServiceClient);
  }

  @Test
  void create_ShouldSaveAndReturnOrderUserDto() {
    OrderDto newDto =
        new OrderDto(null, 100L, StatusEnum.PENDING, LocalDate.now().minusDays(1), "new@test.com");
    Order savedEntity = new Order();
    OrderDto savedDto =
        new OrderDto(2L, 100L, StatusEnum.PENDING, LocalDate.now().minusDays(1), "new@test.com");

    when(orderMapper.toEntity(newDto)).thenReturn(mockOrderEntity);
    when(orderRepository.save(mockOrderEntity)).thenReturn(savedEntity);
    when(orderMapper.toDto(savedEntity)).thenReturn(savedDto);
    when(userServiceClient.getUserByEmail(newDto.userEmail())).thenReturn(mockUserDto);

    OrderUserDto result = orderService.create(newDto);

    assertThat(result.orderDto().id()).isEqualTo(2L);
    verify(orderRepository).save(mockOrderEntity);
  }

  @Test
  void updateById_ShouldReturnUpdatedOrderUserDto_OnSuccess() {
    Long orderId = 1L;
    OrderDto updateDto =
        new OrderDto(orderId, 100L, StatusEnum.SHIPPED, LocalDate.now(), "test@example.com");

    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    when(orderRepository.updateById(orderId, updateDto.status())).thenReturn(1);
    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(mockOrderEntity));

    when(orderMapper.toDto(any(Order.class))).thenReturn(updateDto);
    when(userServiceClient.getUserByEmail(updateDto.userEmail())).thenReturn(mockUserDto);

    orderService.updateById(orderId, updateDto);

    verify(orderRepository, times(2)).findOrderById(orderId);
    verify(orderRepository).updateById(orderId, updateDto.status());
  }

  @Test
  void updateById_ShouldThrowNotFoundException_WhenOrderToUpdateNotFound() {
    Long orderId = 99L;
    OrderDto updateDto =
        new OrderDto(orderId, 100L, StatusEnum.SHIPPED, LocalDate.now(), "test@example.com");

    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.updateById(orderId, updateDto))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void updateById_ShouldThrowUpdateException_WhenNoRowsAffected() {
    Long orderId = 1L;
    OrderDto updateDto =
        new OrderDto(orderId, 100L, StatusEnum.SHIPPED, LocalDate.now(), "test@example.com");

    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    when(orderRepository.updateById(orderId, updateDto.status())).thenReturn(0);

    assertThatThrownBy(() -> orderService.updateById(orderId, updateDto))
        .isInstanceOf(UpdateException.class)
        .hasMessageContaining(String.valueOf(orderId));
  }

  @Test
  void deleteById_ShouldDeleteOrder_OnSuccess() {
    Long orderId = 1L;

    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    doNothing().when(orderRepository).deleteById(orderId);

    orderService.deleteById(orderId);

    verify(orderRepository).findOrderById(orderId);
    verify(orderRepository).deleteById(orderId);
  }

  @Test
  void deleteById_ShouldThrowNotFoundException_WhenOrderToDeleteNotFound() {
    Long orderId = 99L;
    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.deleteById(orderId))
        .isInstanceOf(NotFoundException.class);

    verify(orderRepository).findOrderById(orderId);
    verify(orderRepository, never()).deleteById(any());
  }

  @Test
  void findById_ShouldThrowRetrieveUserException_WhenUserClientFails_NotFound() {
    Long orderId = 1L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(mockOrderDto.userEmail()))
        .thenThrow(mock(FeignException.NotFound.class));

    assertThatThrownBy(() -> orderService.findById(orderId))
        .isInstanceOf(RetrieveUserException.class)
        .hasMessageContaining("User with email 'test@example.com' not found");
  }

  @Test
  void findById_ShouldThrowRetrieveUserException_WhenUserClientFails_GenericFeign() {
    Long orderId = 1L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(mockOrderDto.userEmail()))
        .thenThrow(mock(FeignException.class));

    assertThatThrownBy(() -> orderService.findById(orderId))
        .isInstanceOf(RetrieveUserException.class)
        .hasMessageContaining("Communication error with User Service");
  }

  @Test
  void findById_ShouldThrowRetrieveUserException_WhenUserClientFails_UnexpectedError() {
    Long orderId = 1L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(mockOrderDto.userEmail()))
        .thenThrow(new RuntimeException("Test unexpected error"));

    assertThatThrownBy(() -> orderService.findById(orderId))
        .isInstanceOf(RetrieveUserException.class)
        .hasMessageContaining("An unexpected error occurred while retrieving user");
  }

  @Test
  void findAll_ShouldCallFindBySpecificationWithUnrestricted() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Order> mockPage = new PageImpl<>(List.of(mockOrderEntity), pageable, 1);

    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);
    when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(mockOrderDto.userEmail())).thenReturn(mockUserDto);

    orderService.findAll(pageable);

    verify(orderRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void findBySpecification_ShouldReturnPagedOrderUserDto() {
    Pageable pageable = PageRequest.of(0, 10);
    Specification<Order> dummySpec = Specification.unrestricted();
    Page<Order> mockPage = new PageImpl<>(List.of(mockOrderEntity), pageable, 1);

    when(orderRepository.findAll(eq(dummySpec), eq(pageable))).thenReturn(mockPage);
    when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(mockOrderDto.userEmail())).thenReturn(mockUserDto);

    Page<OrderUserDto> result = orderService.findBySpecification(dummySpec, pageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(orderRepository).findAll(eq(dummySpec), eq(pageable));
  }

  @Test
  void create_ShouldThrowRetrieveUserException_WhenUserClientFails() {
    OrderDto newDto =
        new OrderDto(null, 100L, StatusEnum.PENDING, LocalDate.now().minusDays(1), "new@test.com");

    when(orderMapper.toEntity(newDto)).thenReturn(mockOrderEntity);
    when(orderRepository.save(mockOrderEntity)).thenReturn(mockOrderEntity);
    when(orderMapper.toDto(mockOrderEntity)).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(newDto.userEmail()))
        .thenThrow(mock(FeignException.NotFound.class));

    assertThatThrownBy(() -> orderService.create(newDto))
        .isInstanceOf(RetrieveUserException.class);

    verify(orderRepository).save(mockOrderEntity);
  }

  @Test
  void create_ShouldThrowRetrieveUserException_WhenUserClientFailsWithGenericFeign() {
    OrderDto newDto =
        new OrderDto(null, 100L, StatusEnum.PENDING, LocalDate.now().minusDays(1), "new@test.com");

    when(orderMapper.toEntity(newDto)).thenReturn(mockOrderEntity);
    when(orderRepository.save(mockOrderEntity)).thenReturn(mockOrderEntity);
    when(orderMapper.toDto(mockOrderEntity)).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(newDto.userEmail()))
        .thenThrow(mock(FeignException.class));

    assertThatThrownBy(() -> orderService.create(newDto))
        .isInstanceOf(RetrieveUserException.class);

    verify(orderRepository).save(mockOrderEntity);
  }

  @Test
  void create_ShouldThrowRetrieveUserException_WhenUserClientFailsWithUnexpectedError() {
    OrderDto newDto =
        new OrderDto(null, 100L, StatusEnum.PENDING, LocalDate.now().minusDays(1), "new@test.com");

    when(orderMapper.toEntity(newDto)).thenReturn(mockOrderEntity);
    when(orderRepository.save(mockOrderEntity)).thenReturn(mockOrderEntity);
    when(orderMapper.toDto(mockOrderEntity)).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(newDto.userEmail()))
        .thenThrow(new RuntimeException("Unexpected database error"));

    assertThatThrownBy(() -> orderService.create(newDto))
        .isInstanceOf(RetrieveUserException.class)
        .hasMessageContaining("An unexpected error occurred while retrieving user");

    verify(orderRepository).save(mockOrderEntity);
  }

  @Test
  void updateById_ShouldThrowRetrieveUserException_WhenUserClientFails() {
    Long orderId = 1L;
    OrderDto updateDto =
        new OrderDto(orderId, 100L, StatusEnum.SHIPPED, LocalDate.now(), "test@example.com");

    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    when(orderRepository.updateById(orderId, updateDto.status())).thenReturn(1);
    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    when(orderMapper.toDto(any(Order.class))).thenReturn(updateDto);
    when(userServiceClient.getUserByEmail(updateDto.userEmail()))
        .thenThrow(mock(FeignException.NotFound.class));

    assertThatThrownBy(() -> orderService.updateById(orderId, updateDto))
        .isInstanceOf(RetrieveUserException.class)
        .hasMessageContaining("User with email 'test@example.com' not found");

    verify(orderRepository).updateById(orderId, updateDto.status());
  }

  @Test
  void updateById_ShouldThrowRetrieveUserException_WhenUserClientFailsAfterSuccessfulUpdate() {
    Long orderId = 1L;
    OrderDto updateDto =
        new OrderDto(orderId, 100L, StatusEnum.SHIPPED, LocalDate.now(), "test@example.com");
    Order updatedOrder = new Order();

    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    when(orderRepository.updateById(orderId, updateDto.status())).thenReturn(1);
    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(updatedOrder));
    when(orderMapper.toDto(updatedOrder)).thenReturn(updateDto);
    when(userServiceClient.getUserByEmail(updateDto.userEmail()))
        .thenThrow(new RuntimeException("Network error"));

    assertThatThrownBy(() -> orderService.updateById(orderId, updateDto))
        .isInstanceOf(RetrieveUserException.class)
        .hasMessageContaining("An unexpected error occurred while retrieving user");

    verify(orderRepository).updateById(orderId, updateDto.status());
  }

  @Test
  void findAll_ShouldThrowRetrieveUserException_WhenUserClientFails() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Order> mockPage = new PageImpl<>(List.of(mockOrderEntity), pageable, 1);

    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);
    when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(mockOrderDto.userEmail()))
        .thenThrow(mock(FeignException.NotFound.class));

    assertThatThrownBy(() -> orderService.findAll(pageable))
        .isInstanceOf(RetrieveUserException.class)
        .hasMessageContaining("User with email 'test@example.com' not found");
  }

  @Test
  void findBySpecification_ShouldThrowRetrieveUserException_WhenUserClientFails() {
    Pageable pageable = PageRequest.of(0, 10);
    Specification<Order> dummySpec = Specification.unrestricted();
    Page<Order> mockPage = new PageImpl<>(List.of(mockOrderEntity), pageable, 1);

    when(orderRepository.findAll(eq(dummySpec), eq(pageable))).thenReturn(mockPage);
    when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDto);
    when(userServiceClient.getUserByEmail(mockOrderDto.userEmail()))
        .thenThrow(mock(FeignException.class));

    assertThatThrownBy(() -> orderService.findBySpecification(dummySpec, pageable))
        .isInstanceOf(RetrieveUserException.class)
        .hasMessageContaining("Communication error with User Service");
  }

  @Test
  void findBySpecification_ShouldPropagateException_WhenRepositoryFails() {
    Pageable pageable = PageRequest.of(0, 10);
    Specification<Order> dummySpec = Specification.unrestricted();

    when(orderRepository.findAll(eq(dummySpec), eq(pageable)))
        .thenThrow(new RuntimeException("Database connection failed"));

    assertThatThrownBy(() -> orderService.findBySpecification(dummySpec, pageable))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Database connection failed");

    verifyNoInteractions(userServiceClient);
  }

  @Test
  void create_ShouldPropagateException_WhenRepositorySaveFails() {
    OrderDto newDto =
        new OrderDto(null, 100L, StatusEnum.PENDING, LocalDate.now().minusDays(1), "new@test.com");

    when(orderMapper.toEntity(newDto)).thenReturn(mockOrderEntity);
    when(orderRepository.save(mockOrderEntity))
        .thenThrow(new RuntimeException("Database constraint violation"));

    assertThatThrownBy(() -> orderService.create(newDto))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Database constraint violation");

    verifyNoInteractions(userServiceClient);
  }

  @Test
  void deleteById_ShouldPropagateException_WhenRepositoryDeleteFails() {
    Long orderId = 1L;

    when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(mockOrderEntity));
    doThrow(new RuntimeException("Foreign key constraint"))
        .when(orderRepository)
        .deleteById(orderId);

    assertThatThrownBy(() -> orderService.deleteById(orderId))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Foreign key constraint");

    verify(orderRepository).deleteById(orderId);
  }
}

package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.exception.CircuitBreakerOpenException;
import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.exception.RetrieveUserException;
import com.innowise.orderservice.exception.UpdateException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.messaging.OrderEventProducer;
import com.innowise.orderservice.messaging.event.OrderCreatedEvent;
import com.innowise.orderservice.messaging.event.PaymentCreatedEvent;
import com.innowise.orderservice.model.StatusEnum;
import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.dto.OrderItemDto;
import com.innowise.orderservice.model.dto.OrderUserDto;
import com.innowise.orderservice.model.dto.UserDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.CrudService;
import com.innowise.orderservice.util.OrderSpecifications;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements CrudService<OrderDto, OrderUserDto, Long> {
  private final OrderRepository orderRepository;
  private final ItemRepository itemRepository;
  private final OrderMapper orderMapper;
  private final UserServiceClient userServiceClient;
  private final OrderEventProducer orderEventProducer;

  @Transactional
  @Override
  public OrderUserDto create(OrderDto dto) {
    combineWithUser(dto);

    Order entity = orderMapper.toEntity(dto);
    BigDecimal totalAmount = countTotalCost(dto, entity);

    Order saved = orderRepository.save(entity);

    orderEventProducer.publishOrderCreated(
        new OrderCreatedEvent(saved.getId(), saved.getUserId(), totalAmount, Instant.now()));

    OrderDto savedDto = orderMapper.toDto(saved);
    return combineWithUser(savedDto);
  }

  @Transactional(readOnly = true)
  @Override
  public OrderUserDto findById(Long id) {
    return combineWithUser(
        orderMapper.toDto(
            orderRepository.findById(id).orElseThrow(() -> new NotFoundException(id))));
  }

  @Transactional(readOnly = true)
  @Override
  public Page<OrderUserDto> findAll(Pageable pageable) {
    return findBySpecification(OrderSpecifications.all(), pageable);
  }

  public Page<OrderUserDto> findBySpecification(
      Specification<Order> specification, Pageable pageable) {
    return orderRepository
        .findAll(specification, pageable)
        .map(orderMapper::toDto)
        .map(this::combineWithUser);
  }

  @Transactional
  @Override
  public OrderUserDto updateById(Long id, OrderDto dto) {
    if (!orderRepository.existsById(id)) {
      throw new UpdateException(id, new NotFoundException(id));
    }

    int updated = orderRepository.updateById(id, dto.status());

    if (updated == 0) {
      throw new UpdateException(id);
    }

    OrderDto updatedDto =
        orderMapper.toDto(
            orderRepository.findOrderById(id).orElseThrow(() -> new NotFoundException(id)));
    return combineWithUser(updatedDto);
  }

  @Transactional
  @Override
  public void deleteById(Long id) {
    orderRepository.findOrderById(id).orElseThrow(() -> new NotFoundException(id));
    orderRepository.deleteById(id);
  }

  @Transactional
  public void handlePaymentEvent(PaymentCreatedEvent event) {
    orderRepository
        .findById(event.orderId())
        .ifPresentOrElse(
            order -> {
              StatusEnum targetStatus = mapPaymentStatus(event.status());
              orderRepository.updateById(order.getId(), targetStatus);
              log.debug(
                  "Order {} status updated to {} based on payment {}",
                  order.getId(),
                  targetStatus,
                  event.paymentId());
            },
            () ->
                log.warn(
                    "Order {} not found while handling payment event {}",
                    event.orderId(),
                    event.paymentId()));
  }

  private OrderUserDto combineWithUser(OrderDto orderDto) {
    UserDto userDto = fetchUser(orderDto.userEmail());
    return new OrderUserDto(orderDto, userDto);
  }

  private BigDecimal countTotalCost(OrderDto dto, Order entity) {
    List<OrderItem> orderItems = entity.getOrderItems();
    if (orderItems == null || orderItems.isEmpty()) {
      return BigDecimal.ZERO;
    }

    BigDecimal total = BigDecimal.ZERO;

    for (int index = 0; index < orderItems.size(); ++index) {
      OrderItem orderItem = orderItems.get(index);
      OrderItemDto orderItemDto = dto.orderItems().get(index);

      Item item =
          itemRepository
              .findById(orderItemDto.itemId())
              .orElseThrow(() -> new NotFoundException(orderItemDto.itemId()));

      bindOrderAndItem(entity, orderItem, item);

      total = total.add(item.getPrice().multiply(BigDecimal.valueOf(orderItemDto.quantity())));
    }

    return total;
  }

  private void bindOrderAndItem(Order entity, OrderItem orderItem, Item item) {
    orderItem.setOrder(entity);
    orderItem.setItem(item);
  }

  private StatusEnum mapPaymentStatus(String paymentStatus) {
    return "SUCCESS".equalsIgnoreCase(paymentStatus) ? StatusEnum.CONFIRMED : StatusEnum.PAYMENT_FAILED;
  }

  private UserDto fetchUser(String email) {
    try {
      return userServiceClient.getUserByEmail("email", email).getFirst();
    } catch (CircuitBreakerOpenException e) {
      throw new RetrieveUserException(
          "User Service is currently unavailable. Please try again later.", e);
    } catch (FeignException.NotFound notFound) {
      throw new RetrieveUserException("User with email '%s' not found".formatted(email));
    } catch (FeignException e) {
      throw new RetrieveUserException(
          "Communication error with User Service: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new RetrieveUserException(
          "An unexpected error occurred while retrieving user: " + e.getMessage(), e);
    }
  }
}

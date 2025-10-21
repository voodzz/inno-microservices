package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.exception.RetrieveUserException;
import com.innowise.orderservice.exception.UpdateException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.dto.OrderUserDto;
import com.innowise.orderservice.model.dto.UserDto;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.CrudService;
import com.innowise.orderservice.util.OrderSpecifications;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService implements CrudService<OrderDto, OrderUserDto, Long> {
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;
  private final UserServiceClient userServiceClient;

  @Transactional
  @Override
  public OrderUserDto create(OrderDto dto) {
    combineWithUser(dto);

    Order entity = orderMapper.toEntity(dto);
    Order saved = orderRepository.save(entity);
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
    orderRepository.findOrderById(id).orElseThrow(() -> new NotFoundException(id));

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

  private OrderUserDto combineWithUser(OrderDto orderDto) {
    try {
      UserDto userDto;
      userDto = userServiceClient.getUserByEmail(orderDto.userEmail());
      return new OrderUserDto(orderDto, userDto);
    } catch (FeignException.NotFound notFound) {
      throw new RetrieveUserException(
          "User with email '%s' not found".formatted(orderDto.userEmail()));
    } catch (FeignException e) {
      throw new RetrieveUserException(
          "Communication error with User Service: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new RetrieveUserException(
          "An unexpected error occurred while retrieving user: " + e.getMessage(), e);
    }
  }
}

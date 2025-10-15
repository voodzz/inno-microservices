package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.exception.UpdateException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.StatusEnum;
import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class OrderService implements CrudService<OrderDto, Long> {
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  @Override
  public OrderDto create(OrderDto dto) {
    Order entity = orderMapper.toEntity(dto);
    return orderMapper.toDto(orderRepository.save(entity));
  }

  @Override
  public OrderDto findById(Long id) {
    return orderMapper.toDto(
        orderRepository.findById(id).orElseThrow(() -> new NotFoundException(id)));
  }

  @Override
  public Page<OrderDto> findByIds(Collection<Long> ids, Pageable pageable) {
    return orderRepository.findOrdersByIdIn(ids, pageable).map(orderMapper::toDto);
  }

  public Page<OrderDto> findByStatuses(Collection<StatusEnum> statuses, Pageable pageable) {
    return orderRepository.findOrdersByStatusIn(statuses, pageable).map(orderMapper::toDto);
  }

  @Override
  public Page<OrderDto> findAll(Pageable pageable) {
    return orderRepository.findAll(pageable).map(orderMapper::toDto);
  }

  @Override
  public void updateById(Long id, OrderDto dto) {
    orderRepository.findOrderById(id).orElseThrow(() -> new NotFoundException(id));

    int updated = orderRepository.updateById(id, dto.status());

    if (updated == 0) {
      throw new UpdateException(id);
    }
  }

  @Override
  public void deleteById(Long id) {
    orderRepository.findOrderById(id).orElseThrow(() -> new NotFoundException(id));
    orderRepository.deleteById(id);
  }
}

package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.dto.OrderItemDto;
import com.innowise.orderservice.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Mapper interface for converting between {@link OrderItem} entities and {@link OrderItemDto}
 * objects.
 *
 * <p>Implementations are generated automatically by MapStruct at compile time. This mapper is
 * configured as a Spring component for dependency injection.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemMapper {
  /**
   * Converts a {@link OrderItemDto} to a {@link OrderItem} entity
   *
   * @param dto the OrderItem DTO containing data
   * @return the corresponding {@link OrderItem} entity
   */
  OrderItem toEntity(OrderItemDto dto);

  /**
   * Converts a {@link OrderItem} entity to a {@link OrderItemDto}
   *
   * @param item the OrderItem entity to convert
   * @return the corresponding {@link OrderDto}
   */
  OrderItemDto toDto(OrderItem item);

  /**
   * Converts a list of {@link OrderItem} entities to a list of {@link OrderItemDto} objects
   *
   * @param items the list of OrderItem entities to convert
   * @return a list of corresponding {@link OrderItemDto} objects
   */
  List<OrderItemDto> toDtoList(List<OrderItem> items);

  /**
   * Converts a list of {@link OrderItemDto} objects to a list of {@link OrderItem} entities
   *
   * @param dtoList the list of OrderItem DTOs to convert
   * @return a list of corresponding {@link OrderItem} entities
   */
  List<OrderItem> toEntityList(List<OrderItemDto> dtoList);
}

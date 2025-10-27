package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.OrderItemDto;
import com.innowise.orderservice.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
  @Mapping(target = "order", ignore = true)
  @Mapping(target = "item", ignore = true)
  OrderItem toEntity(OrderItemDto dto);

  /**
   * Converts a {@link OrderItem} entity to a {@link OrderItemDto}
   *
   * @param entity the OrderItem entity to convert
   * @return the corresponding {@link OrderItemDto}
   */
  @Mapping(source = "order.id", target = "orderId")
  @Mapping(source = "item.id", target = "itemId")
  OrderItemDto toDto(OrderItem entity);

  /**
   * Converts a list of {@link OrderItemDto} objects to a list of {@link OrderItem} entities
   *
   * @param dtoList the list of OrderItem DTOs to convert
   * @return a list of corresponding {@link OrderItem} entities
   */
  List<OrderItem> toEntityList(List<OrderItemDto> dtoList);

  /**
   * Converts a list of {@link OrderItem} entities to a list of {@link OrderItemDto} objects
   *
   * @param entityList the list of OrderItem entities to convert
   * @return a list of corresponding {@link OrderItemDto} objects
   */
  List<OrderItemDto> toDtoList(List<OrderItem> entityList);
}

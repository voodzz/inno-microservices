package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper interface for converting between {@link Order} entities and {@link OrderDto} objects.
 *
 * <p>Implementations are generated automatically by MapStruct at compile time. This mapper is
 * configured as a Spring component for dependency injection.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

  /**
   * Converts a {@link OrderDto} to a {@link Order} entity
   *
   * @param dto the order DTO containing data
   * @return the corresponding {@link Order} entity
   */
  @Mapping(target = "orderItems", ignore = true)
  Order toEntity(OrderDto dto);

  /**
   * Converts a {@link Order} entity to a {@link OrderDto}
   *
   * @param order the order entity to convert
   * @return the corresponding {@link OrderDto}
   */
  @Mapping(target = "userEmail", ignore = true)
  OrderDto toDto(Order order);

  /**
   * Converts a list of {@link Order} entities to a list of {@link OrderDto} objects
   *
   * @param orders the list of order entities to convert
   * @return a list of corresponding {@link OrderDto} objects
   */
  @Mapping(target = "userEmail", ignore = true)
  List<OrderDto> toDtoList(List<Order> orders);

  /**
   * Converts a list of {@link OrderDto} objects to a list of {@link Order} entities
   *
   * @param dtoList the list of order DTOs to convert
   * @return a list of corresponding {@link Order} entities
   */
  List<Order> toEntityList(List<OrderDto> dtoList);

  /**
   * Updates an existing Order entity from an OrderDto, ignoring the primary key ID from the DTO.
   *
   * @param dto the source OrderDto.
   * @param target the existing Order entity to update.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "orderItems", ignore = true)
  void updateEntityFromDto(OrderDto dto, @MappingTarget Order target);
}

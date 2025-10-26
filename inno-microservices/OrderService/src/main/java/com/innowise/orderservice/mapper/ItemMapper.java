package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.ItemDto;
import com.innowise.orderservice.model.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Mapper interface for converting between {@link Item} entities and {@link ItemDto} objects.
 *
 * <p>Implementations are generated automatically by MapStruct at compile time. This mapper is
 * configured as a Spring component for dependency injection.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {
  /**
   * Converts a {@link ItemDto} to a {@link Item} entity
   *
   * @param dto the item DTO containing data
   * @return the corresponding {@link Item} entity
   */
  @Mapping(target = "orderItems", ignore = true)
  Item toEntity(ItemDto dto);

  /**
   * Converts a {@link Item} entity to a {@link ItemDto}
   *
   * @param entity the item entity to convert
   * @return the corresponding {@link ItemDto}
   */
  ItemDto toDto(Item entity);

  /**
   * Converts a list of {@link ItemDto} objects to a list of {@link Item} entities
   *
   * @param dtoList the list of item DTOs to convert
   * @return a list of corresponding {@link Item} entities
   */
  List<Item> toEntityList(List<ItemDto> dtoList);

  /**
   * Converts a list of {@link Item} entities to a list of {@link ItemDto} objects
   *
   * @param entityList the list of item entities to convert
   * @return a list of corresponding {@link ItemDto} objects
   */
  List<ItemDto> toDtoList(List<Item> entityList);
}

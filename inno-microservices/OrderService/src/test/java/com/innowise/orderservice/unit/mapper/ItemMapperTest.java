package com.innowise.orderservice.unit.mapper;

import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.dto.ItemDto;
import com.innowise.orderservice.model.entity.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemMapperTest {

  private ItemMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(ItemMapper.class);
  }

  private final Long itemId = 1L;
  private final String testName = "Test Item";
  private final BigDecimal testPrice = new BigDecimal("29.99");

  private ItemDto createItemDto(Long id, String name, BigDecimal price) {
    return new ItemDto(id, name, price);
  }

  private Item createItemEntity() {
    Item entity = new Item();
    entity.setId(itemId);
    entity.setName(testName);
    entity.setPrice(testPrice);
    entity.setOrderItems(Collections.emptyList());
    return entity;
  }

  @Test
  void toDto_ShouldMapEntityToDto_WithCorrectFields() {
    Item entity = createItemEntity();

    ItemDto dto = mapper.toDto(entity);

    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo(itemId);
    assertThat(dto.name()).isEqualTo(testName);
    assertThat(dto.price()).isEqualByComparingTo(testPrice);
  }

  @Test
  void toDto_ShouldReturnNull_WhenInputIsNull() {
    assertThat(mapper.toDto(null)).isNull();
  }

  @Test
  void toEntity_ShouldMapDtoToEntity_IgnoringOrderItems() {
    ItemDto dto = createItemDto(null, testName, testPrice);

    Item entity = mapper.toEntity(dto);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(entity.getName()).isEqualTo(testName);
    assertThat(entity.getPrice()).isEqualByComparingTo(testPrice);
    assertThat(entity.getOrderItems()).isEmpty();
  }

  @Test
  void toEntity_ShouldMapDtoWithId_IgnoringOrderItems() {
    ItemDto dto = createItemDto(itemId, testName, testPrice);

    Item entity = mapper.toEntity(dto);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isEqualTo(itemId);
    assertThat(entity.getName()).isEqualTo(testName);
    assertThat(entity.getPrice()).isEqualByComparingTo(testPrice);
    assertThat(entity.getOrderItems()).isEmpty();
  }

  @Test
  void toEntity_ShouldReturnNull_WhenInputIsNull() {
    assertThat(mapper.toEntity(null)).isNull();
  }

  @Test
  void toDtoList_ShouldMapListOfEntitiesToDtoList() {
    Item entity1 = createItemEntity();
    Item entity2 = createItemEntity();
    entity2.setId(2L);
    entity2.setName("Another Item");
    entity2.setPrice(new BigDecimal("49.99"));

    List<ItemDto> dtoList = mapper.toDtoList(List.of(entity1, entity2));

    assertThat(dtoList).isNotNull();
    assertThat(dtoList).hasSize(2);
    assertThat(dtoList.get(0).id()).isEqualTo(itemId);
    assertThat(dtoList.get(0).name()).isEqualTo(testName);
    assertThat(dtoList.get(1).id()).isEqualTo(2L);
    assertThat(dtoList.get(1).name()).isEqualTo("Another Item");
  }

  @Test
  void toEntityList_ShouldMapListOfDtosToEntityList() {
    ItemDto dto1 = createItemDto(null, testName, testPrice);
    ItemDto dto2 = createItemDto(null, "Second Item", new BigDecimal("99.99"));

    List<Item> entityList = mapper.toEntityList(List.of(dto1, dto2));

    assertThat(entityList).isNotNull();
    assertThat(entityList).hasSize(2);
    assertThat(entityList.get(0).getName()).isEqualTo(testName);
    assertThat(entityList.get(0).getPrice()).isEqualByComparingTo(testPrice);
    assertThat(entityList.get(1).getName()).isEqualTo("Second Item");
  }

  @Test
  void toDtoList_ShouldReturnNull_WhenInputIsNull() {
    assertThat(mapper.toDtoList(null)).isNull();
  }

  @Test
  void toEntityList_ShouldReturnNull_WhenInputIsNull() {
    assertThat(mapper.toEntityList(null)).isNull();
  }

  @Test
  void toDtoList_ShouldHandleEmptyList() {
    List<ItemDto> dtoList = mapper.toDtoList(Collections.emptyList());

    assertThat(dtoList).isNotNull();
    assertThat(dtoList).isEmpty();
  }

  @Test
  void toEntityList_ShouldHandleEmptyList() {
    List<Item> entityList = mapper.toEntityList(Collections.emptyList());

    assertThat(entityList).isNotNull();
    assertThat(entityList).isEmpty();
  }

  @Test
  void toDto_ShouldHandleDifferentPriceValues() {
    Item entity = createItemEntity();
    entity.setPrice(new BigDecimal("0.01"));
    ItemDto dto = mapper.toDto(entity);

    assertThat(dto.price()).isEqualByComparingTo(new BigDecimal("0.01"));

    entity.setPrice(new BigDecimal("99999.99"));
    dto = mapper.toDto(entity);
    assertThat(dto.price()).isEqualByComparingTo(new BigDecimal("99999.99"));
  }

  @Test
  void toEntity_ShouldHandleDifferentPriceValues() {
    ItemDto dto1 = createItemDto(null, testName, new BigDecimal("0.01"));
    ItemDto dto2 = createItemDto(null, testName, new BigDecimal("99999.99"));

    Item entity1 = mapper.toEntity(dto1);
    Item entity2 = mapper.toEntity(dto2);

    assertThat(entity1.getPrice()).isEqualByComparingTo(new BigDecimal("0.01"));
    assertThat(entity2.getPrice()).isEqualByComparingTo(new BigDecimal("99999.99"));
  }

  @Test
  void toDto_ShouldHandleSpecialCharactersInName() {
    Item entity = createItemEntity();
    entity.setName("Item with émoji 😀 & symbols!");
    ItemDto dto = mapper.toDto(entity);

    assertThat(dto.name()).isEqualTo("Item with émoji 😀 & symbols!");
  }

  @Test
  void toEntity_ShouldHandleSpecialCharactersInName() {
    ItemDto dto = createItemDto(null, "Item with émoji 😀 & symbols!", testPrice);
    Item entity = mapper.toEntity(dto);

    assertThat(entity.getName()).isEqualTo("Item with émoji 😀 & symbols!");
  }
}

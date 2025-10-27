package com.innowise.orderservice.unit.mapper;

import com.innowise.orderservice.mapper.OrderItemMapper;
import com.innowise.orderservice.model.dto.OrderItemDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderItemMapperTest {

  private OrderItemMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(OrderItemMapper.class);
  }

  private final Long orderItemId = 1L;
  private final Long testOrderId = 10L;
  private final Long testItemId = 20L;
  private final Integer testQuantity = 5;

  private OrderItemDto createOrderItemDto(Long id, Long orderId, Long itemId, Integer quantity) {
    return new OrderItemDto(id, orderId, itemId, quantity);
  }

  private OrderItem createOrderItemEntity() {
    OrderItem entity = new OrderItem();
    entity.setId(orderItemId);
    entity.setQuantity(testQuantity);

    Order order = new Order();
    order.setId(testOrderId);
    entity.setOrder(order);

    Item item = new Item();
    item.setId(testItemId);
    entity.setItem(item);

    return entity;
  }

  @Test
  void toDto_ShouldMapEntityToDto_WithCorrectMappings() {
    OrderItem entity = createOrderItemEntity();

    OrderItemDto dto = mapper.toDto(entity);

    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo(orderItemId);
    assertThat(dto.orderId()).isEqualTo(testOrderId);
    assertThat(dto.itemId()).isEqualTo(testItemId);
    assertThat(dto.quantity()).isEqualTo(testQuantity);
  }

  @Test
  void toDto_ShouldReturnNull_WhenInputIsNull() {
    assertThat(mapper.toDto(null)).isNull();
  }

  @Test
  void toEntity_ShouldMapDtoToEntity_IgnoringRelations() {
    OrderItemDto dto = createOrderItemDto(null, testOrderId, testItemId, testQuantity);

    OrderItem entity = mapper.toEntity(dto);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(entity.getQuantity()).isEqualTo(testQuantity);
    assertThat(entity.getOrder()).isNull();
    assertThat(entity.getItem()).isNull();
  }

  @Test
  void toEntity_ShouldMapDtoWithId_IgnoringRelations() {
    OrderItemDto dto = createOrderItemDto(orderItemId, testOrderId, testItemId, testQuantity);

    OrderItem entity = mapper.toEntity(dto);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isEqualTo(orderItemId);
    assertThat(entity.getQuantity()).isEqualTo(testQuantity);
    assertThat(entity.getOrder()).isNull();
    assertThat(entity.getItem()).isNull();
  }

  @Test
  void toEntity_ShouldReturnNull_WhenInputIsNull() {
    assertThat(mapper.toEntity(null)).isNull();
  }

  @Test
  void toDtoList_ShouldMapListOfEntitiesToDtoList() {
    OrderItem entity1 = createOrderItemEntity();
    OrderItem entity2 = createOrderItemEntity();
    entity2.setId(2L);

    List<OrderItemDto> dtoList = mapper.toDtoList(List.of(entity1, entity2));

    assertThat(dtoList).isNotNull();
    assertThat(dtoList).hasSize(2);
    assertThat(dtoList.get(0).id()).isEqualTo(orderItemId);
    assertThat(dtoList.get(1).id()).isEqualTo(2L);
  }

  @Test
  void toEntityList_ShouldMapListOfDtosToEntityList() {
    OrderItemDto dto1 = createOrderItemDto(null, testOrderId, testItemId, testQuantity);
    OrderItemDto dto2 = createOrderItemDto(null, testOrderId, testItemId, 10);

    List<OrderItem> entityList = mapper.toEntityList(List.of(dto1, dto2));

    assertThat(entityList).isNotNull();
    assertThat(entityList).hasSize(2);
    assertThat(entityList.get(0).getQuantity()).isEqualTo(testQuantity);
    assertThat(entityList.get(1).getQuantity()).isEqualTo(10);
    assertThat(entityList.get(0).getOrder()).isNull();
    assertThat(entityList.get(0).getItem()).isNull();
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
    List<OrderItemDto> dtoList = mapper.toDtoList(Collections.emptyList());

    assertThat(dtoList).isNotNull();
    assertThat(dtoList).isEmpty();
  }

  @Test
  void toEntityList_ShouldHandleEmptyList() {
    List<OrderItem> entityList = mapper.toEntityList(Collections.emptyList());

    assertThat(entityList).isNotNull();
    assertThat(entityList).isEmpty();
  }
}


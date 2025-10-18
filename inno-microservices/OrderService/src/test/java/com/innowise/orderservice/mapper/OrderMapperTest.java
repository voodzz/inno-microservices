package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.StatusEnum;
import com.innowise.orderservice.model.dto.OrderDto;
import com.innowise.orderservice.model.entity.Order;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

  private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

  private final LocalDate testDate = LocalDate.of(2023, 10, 18);
  private final Long orderId = 1L;
  private final Long testUserId = 100L;
  private final String testEmail = "test@example.com";

  private Order createOrderEntity() {
    Order entity = new Order();
    entity.setId(orderId);
    entity.setUserId(testUserId);
    entity.setStatus(StatusEnum.PENDING);
    entity.setCreationDate(testDate);
    entity.setOrderItems(Collections.emptyList());
    return entity;
  }

  private OrderDto createOrderDto(Long id, StatusEnum status) {
    return new OrderDto(id, testUserId, status, testDate, testEmail);
  }

  @Test
  void toDto_ShouldMapEntityToDto_AndSetUserEmailNull() {
    Order entity = createOrderEntity();

    OrderDto dto = mapper.toDto(entity);

    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo(orderId);
    assertThat(dto.userId()).isEqualTo(testUserId);
    assertThat(dto.userEmail()).isNull();
  }

  @Test
  void toDto_ShouldReturnNull_WhenInputIsNull() {
    assertThat(mapper.toDto(null)).isNull();
  }

  @Test
  void toEntity_ShouldMapDtoToEntity_IgnoringUserEmail() {
    OrderDto dto = createOrderDto(null, StatusEnum.SHIPPED);

    Order entity = mapper.toEntity(dto);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isNull();
    assertThat(entity.getUserId()).isEqualTo(testUserId);
    assertThat(entity.getOrderItems()).isNotNull();
  }

  @Test
  void updateEntityFromDto_ShouldUpdateFields_AndIgnoreIdAndUserEmail() {
    Order existingEntity = createOrderEntity();
    existingEntity.setStatus(StatusEnum.PENDING);

    OrderDto updateDto = createOrderDto(5L, StatusEnum.CONFIRMED);

    mapper.updateEntityFromDto(updateDto, existingEntity);

    assertThat(existingEntity.getId()).isEqualTo(orderId);
    assertThat(existingEntity.getStatus()).isEqualTo(StatusEnum.CONFIRMED);
    assertThat(existingEntity.getUserId()).isEqualTo(updateDto.userId());
    assertThat(existingEntity.getCreationDate()).isEqualTo(updateDto.creationDate());
  }

  @Test
  void toDtoList_ShouldMapListOfEntitiesToDtoList() {
    List<Order> entities = List.of(createOrderEntity());

    List<OrderDto> dtoList = mapper.toDtoList(entities);

    assertThat(dtoList).hasSize(1);
    assertThat(dtoList.get(0).id()).isEqualTo(orderId);
  }

  @Test
  void toEntityList_ShouldMapListOfDtosToEntityList() {
    List<OrderDto> dtoList = List.of(createOrderDto(null, StatusEnum.PENDING));

    List<Order> entities = mapper.toEntityList(dtoList);

    assertThat(entities).hasSize(1);
    assertThat(entities.get(0).getUserId()).isEqualTo(testUserId);
  }

  @Test
  void toDtoList_ShouldReturnNull_WhenInputIsNull() {
    assertThat(mapper.toDtoList(null)).isNull();
  }

  @Test
  void toEntityList_ShouldReturnNull_WhenInputIsNull() {
    assertThat(mapper.toEntityList(null)).isNull();
  }
}

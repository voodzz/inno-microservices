package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * MapStruct mapper interface for converting between the Payment entity and its DTO representation.
 * The implementation is generated at compile time and registered as a Spring component.
 */
@Mapper(componentModel = SPRING)
public interface PaymentMapper {
  /**
   * Converts a Payment Data Transfer Object (DTO) to its corresponding Payment entity.
   *
   * @param dto The DTO to convert.
   * @return The resulting Payment entity.
   */
  Payment toEntity(PaymentDto dto);

  /**
   * Converts a Payment entity to its corresponding Data Transfer Object (DTO).
   *
   * @param entity The entity to convert.
   * @return The resulting PaymentDto.
   */
  PaymentDto toDto(Payment entity);

  /**
   * Converts a list of Payment DTOs to a list of Payment entities.
   *
   * @param entityList The list of DTOs to convert.
   * @return A new list of Payment entities.
   */
  List<Payment> toEntityList(List<PaymentDto> entityList);

  /**
   * Converts a list of Payment entities to a list of Payment DTOs.
   *
   * @param dtoList The list of entities to convert.
   * @return A new list of Payment DTOs.
   */
  List<PaymentDto> toDtoList(List<Payment> dtoList);
}

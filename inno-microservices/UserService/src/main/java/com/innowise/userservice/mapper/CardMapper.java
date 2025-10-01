package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.CardDto;
import com.innowise.userservice.model.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Mapper interface for converting between {@link Card} entities and {@link CardDto} objects.
 *
 * <p>Implementations are generated automatically by MapStruct at compile time. This mapper is
 * configured as a Spring component for dependency injection.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
  /**
   * Converts a {@link CardDto} to a {@link Card} entity.
   *
   * @param cardRequest the card DTO containing request data
   * @return the corresponding {@link Card} entity
   */
  Card toEntity(CardDto cardRequest);

  /**
   * Converts a {@link Card} entity to a {@link CardDto}.
   *
   * @param card the card entity
   * @return the corresponding {@link CardDto}
   */
  CardDto toDto(Card card);

  /**
   * Converts a list of {@link Card} entities to a list of {@link CardDto} objects.
   *
   * @param cards the list of card entities
   * @return a list of corresponding {@link CardDto} objects
   */
  List<CardDto> toDtoList(List<Card> cards);
}

package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.CardDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

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
  @Mapping(target = "user", source = "userId", qualifiedByName = "mapUserIdToUser")
  Card toEntity(CardDto cardRequest);

  /**
   * Converts a {@link Card} entity to a {@link CardDto}.
   *
   * @param card the card entity
   * @return the corresponding {@link CardDto}
   */
  @Mapping(target = "userId", source = "user.id")
  CardDto toDto(Card card);

  /**
   * Converts a list of {@link Card} entities to a list of {@link CardDto} objects.
   *
   * @param cards the list of card entities
   * @return a list of corresponding {@link CardDto} objects
   */
  List<CardDto> toDtoList(List<Card> cards);

  /**
   * Maps a user ID to a minimal {@code User} object.
   *
   * <p>This method is intended to be used by MapStruct's mapping mechanism (via the {@code @Named}
   * annotation) to set a relationship object when only the user's ID is available in the source
   * object.
   *
   * @param userId The ID of the user to be mapped.
   * @return A new {@code User} object with only the ID field set, or {@code null} if the input ID
   *     is {@code null}.
   */
  @Named("mapUserIdToUser")
  default User mapUserIdToUser(Long userId) {
    if (userId == null) {
      return null;
    }
    User user = new User();
    user.setId(userId);
    return user;
  }
}

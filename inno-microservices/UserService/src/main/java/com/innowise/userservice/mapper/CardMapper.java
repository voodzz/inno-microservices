package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.CardDto;
import com.innowise.userservice.model.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
  Card toEntity(CardDto cardRequest);

  CardDto toDto(Card card);

  List<CardDto> toDtoList(List<Card> cards);
}

package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.CardResponse;
import com.innowise.userservice.model.dto.CreateCardRequest;
import com.innowise.userservice.model.dto.UpdateCardRequest;
import com.innowise.userservice.model.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
  Card toEntity(CreateCardRequest cardRequest);

  Card toEntity(UpdateCardRequest cardRequest);

  CardResponse toDto(Card card);

  List<CardResponse> toDtoList(List<Card> cards);
}

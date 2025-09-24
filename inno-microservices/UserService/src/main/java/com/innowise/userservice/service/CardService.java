package com.innowise.userservice.service;

import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.CardUpdateException;
import com.innowise.userservice.mapper.CardMapper;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.CardResponse;
import com.innowise.userservice.model.dto.CreateCardRequest;
import com.innowise.userservice.model.dto.UpdateCardRequest;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CardService {
  private final CardRepository cardRepository;
  private final CardMapper cardMapper;

  @Transactional
  public CardResponse createCard(CreateCardRequest request) {
    Card card = cardMapper.toEntity(request);
    return cardMapper.toDto(cardRepository.save(card));
  }

  public CardResponse findById(Long id) {
    Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
    return cardMapper.toDto(card);
  }

  public List<CardResponse> findByIds(Collection<Long> ids) {
    return cardMapper.toDtoList(cardRepository.findByIdIn(ids));
  }

  @Transactional
  void updateCard(UpdateCardRequest request) {
    Card card =
        cardRepository
            .findById(request.id())
            .orElseThrow(() -> new CardNotFoundException(request.id()));

    int updated =
        cardRepository.updateById(
            card.getId(), request.userId(), request.holder(), request.expirationDate());

    if (updated == 0) {
      throw new CardUpdateException(card.getId());
    }
  }

  @Transactional
  void deleteCard(Long id) {
    Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException(id));
    cardRepository.delete(card);
  }
}

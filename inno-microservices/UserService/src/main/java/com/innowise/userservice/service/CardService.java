package com.innowise.userservice.service;

import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.exception.UpdateException;
import com.innowise.userservice.mapper.CardMapper;
import com.innowise.userservice.model.dto.CardDto;
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
  public CardDto createCard(CardDto request) {
    Card card = cardMapper.toEntity(request);
    return cardMapper.toDto(cardRepository.save(card));
  }

  public CardDto findById(Long id) {
    Card card = cardRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    return cardMapper.toDto(card);
  }

  public List<CardDto> findByIds(Collection<Long> ids) {
    return cardMapper.toDtoList(cardRepository.findByIdIn(ids));
  }

  @Transactional
  public void updateCard(Long id, CardDto request) {
    Card card =
        cardRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException(id));

    int updated =
        cardRepository.updateById(
            card.getId(), request.userId(), request.holder(), request.expirationDate());

    if (updated == 0) {
      throw new UpdateException(card.getId());
    }
  }

  @Transactional
  public void deleteCard(Long id) {
    Card card = cardRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    cardRepository.delete(card);
  }
}

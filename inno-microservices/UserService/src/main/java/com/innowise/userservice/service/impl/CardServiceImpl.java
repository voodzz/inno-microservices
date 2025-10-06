package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.exception.UpdateException;
import com.innowise.userservice.mapper.CardMapper;
import com.innowise.userservice.model.dto.CardDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CardServiceImpl implements CardService {
  private final CardRepository cardRepository;
  private final CardMapper cardMapper;

  @Override
  @Transactional
  public CardDto create(CardDto request) {
    Card card = cardMapper.toEntity(request);
    return cardMapper.toDto(cardRepository.save(card));
  }

  @Override
  public CardDto findById(Long id) {
    Card card = cardRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    return cardMapper.toDto(card);
  }

  @Override
  public List<CardDto> findByIds(Collection<Long> ids) {
    return cardMapper.toDtoList(cardRepository.findByIdIn(ids));
  }

  @Override
  public List<CardDto> findAll() {
    return cardMapper.toDtoList(cardRepository.findAll());
  }

  @Override
  @Transactional
  public void update(Long id, CardDto request) {
    Card card = cardRepository.findById(id).orElseThrow(() -> new NotFoundException(id));

    int updated =
        cardRepository.updateById(
            card.getId(), request.userId(), request.holder(), request.expirationDate());

    if (updated == 0) {
      throw new UpdateException(card.getId());
    }
  }

  @Override
  @Transactional
  public void delete(Long id) {
    Card card = cardRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    cardRepository.delete(card);
  }
}

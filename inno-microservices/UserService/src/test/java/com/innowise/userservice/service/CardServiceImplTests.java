package com.innowise.userservice.service;

import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.exception.UpdateException;
import com.innowise.userservice.mapper.CardMapper;
import com.innowise.userservice.model.dto.CardDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTests {
  @Mock private CardRepository cardRepository;

  @Mock private CardMapper cardMapper;

  @InjectMocks private CardServiceImpl cardService;

  private final Long CARD_ID = 10L;
  private final Long USER_ID = 1L;
  private CardDto cardDto;
  private Card card;

  @BeforeEach
  void setUp() {
    User mockUser = new User();
    mockUser.setId(USER_ID);

    cardDto =
        new CardDto(CARD_ID, USER_ID, "1111222233334444", "JOHN DOE", LocalDate.now().plusYears(1));

    card = new Card();
    card.setId(CARD_ID);
    card.setUser(mockUser);
    card.setNumber("1111222233334444");
    card.setHolder("JOHN DOE");
    card.setExpirationDate(LocalDate.now().plusYears(1));
  }

  @Test
  void create_ShouldReturnCardDto_WhenCalled() {
    when(cardMapper.toEntity(any(CardDto.class))).thenReturn(card);
    when(cardRepository.save(any(Card.class))).thenReturn(card);
    when(cardMapper.toDto(any(Card.class))).thenReturn(cardDto);

    CardDto result = cardService.create(cardDto);

    assertNotNull(result);
    assertEquals(CARD_ID, result.id());
    verify(cardMapper).toEntity(cardDto);
    verify(cardRepository).save(card);
    verify(cardMapper).toDto(card);
  }

  @Test
  void findById_ShouldReturnCardDto_WhenCardExists() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    when(cardMapper.toDto(any(Card.class))).thenReturn(cardDto);

    CardDto result = cardService.findById(CARD_ID);

    assertNotNull(result);
    assertEquals(CARD_ID, result.id());
    verify(cardRepository).findById(CARD_ID);
    verify(cardMapper).toDto(card);
  }

  @Test
  void findById_ShouldThrowNotFoundException_WhenCardDoesNotExist() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> cardService.findById(CARD_ID));

    verify(cardRepository).findById(CARD_ID);
    verify(cardMapper, never()).toDto(any(Card.class));
  }

  @Test
  void findByIds_ShouldReturnListOfCardDtos_WhenCardsExist() {
    List<Long> cardIds = List.of(CARD_ID, 11L);
    List<Card> cardList = List.of(card, new Card());
    List<CardDto> dtoList =
        List.of(cardDto, new CardDto(11L, 2L, "...", "...", LocalDate.now().plusYears(1)));

    when(cardRepository.findByIdIn(cardIds)).thenReturn(cardList);
    when(cardMapper.toDtoList(cardList)).thenReturn(dtoList);

    List<CardDto> result = cardService.findByIds(cardIds);

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(cardRepository).findByIdIn(cardIds);
    verify(cardMapper).toDtoList(cardList);
  }

  @Test
  void findByIds_ShouldReturnEmptyList_WhenNoCardsFound() {
    List<Long> cardIds = List.of(99L);
    List<Card> emptyCardList = Collections.emptyList();
    List<CardDto> emptyDtoList = Collections.emptyList();

    when(cardRepository.findByIdIn(cardIds)).thenReturn(emptyCardList);
    when(cardMapper.toDtoList(emptyCardList)).thenReturn(emptyDtoList);

    List<CardDto> result = cardService.findByIds(cardIds);

    assertTrue(result.isEmpty());
    verify(cardRepository).findByIdIn(cardIds);
  }

  @Test
  void findAll_ShouldReturnAllCardDtos() {
    List<Card> cardList = List.of(card, new Card());
    List<CardDto> dtoList =
        List.of(cardDto, new CardDto(11L, 2L, "...", "...", LocalDate.now().plusYears(1)));

    when(cardRepository.findAll()).thenReturn(cardList);
    when(cardMapper.toDtoList(cardList)).thenReturn(dtoList);

    List<CardDto> result = cardService.findAll();

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(cardRepository).findAll();
    verify(cardMapper).toDtoList(cardList);
  }

  @Test
  void findAll_ShouldReturnEmptyList_WhenNoCardsExist() {
    List<Card> emptyCardList = Collections.emptyList();
    List<CardDto> emptyDtoList = Collections.emptyList();

    when(cardRepository.findAll()).thenReturn(emptyCardList);
    when(cardMapper.toDtoList(emptyCardList)).thenReturn(emptyDtoList);

    List<CardDto> result = cardService.findAll();

    assertTrue(result.isEmpty());
    verify(cardRepository).findAll();
  }

  @Test
  void update_ShouldUpdateCard_WhenCardExists() {
    CardDto updateRequest =
        new CardDto(CARD_ID, USER_ID, "1111222233334444", "JANE DOE", LocalDate.now().plusYears(2));

    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    when(cardRepository.updateById(
            eq(CARD_ID), eq(USER_ID), eq("JANE DOE"), eq(updateRequest.expirationDate())))
        .thenReturn(1);

    cardService.update(CARD_ID, updateRequest);

    verify(cardRepository).findById(CARD_ID);
    verify(cardRepository)
        .updateById(eq(CARD_ID), eq(USER_ID), eq("JANE DOE"), eq(updateRequest.expirationDate()));
  }

  @Test
  void update_ShouldThrowNotFoundException_WhenCardDoesNotExist() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> cardService.update(CARD_ID, cardDto));

    verify(cardRepository).findById(CARD_ID);
    verify(cardRepository, never()).updateById(any(), any(), any(), any());
  }

  @Test
  void update_ShouldThrowUpdateException_WhenNoRowsUpdated() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    when(cardRepository.updateById(any(), any(), any(), any(LocalDate.class))).thenReturn(0);

    assertThrows(UpdateException.class, () -> cardService.update(CARD_ID, cardDto));

    verify(cardRepository).findById(CARD_ID);
    verify(cardRepository).updateById(any(), any(), any(), any());
  }

  @Test
  void delete_ShouldDeleteCard_WhenCardExists() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    doNothing().when(cardRepository).delete(card);

    cardService.delete(CARD_ID);

    verify(cardRepository).findById(CARD_ID);
    verify(cardRepository).delete(card);
  }

  @Test
  void delete_ShouldThrowNotFoundException_WhenCardDoesNotExist() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> cardService.delete(CARD_ID));

    verify(cardRepository).findById(CARD_ID);
    verify(cardRepository, never()).delete(any(Card.class));
  }
}

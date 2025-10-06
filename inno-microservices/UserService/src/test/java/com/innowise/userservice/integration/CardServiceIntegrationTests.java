package com.innowise.userservice.integration;

import com.innowise.userservice.model.dto.CardDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class CardServiceIntegrationTests extends IntegrationTestBase {

  @Autowired private CardService cardService;

  @Autowired private CardRepository cardRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void setup() {
    cardRepository.deleteAll();

    testUser = new User();
    testUser.setName("Test");
    testUser.setSurname("User");
    testUser.setEmail("user@example.com");
    testUser.setBirthDate(LocalDate.of(1990, 1, 1));
    testUser = userRepository.save(testUser);
  }

  @Test
  void create_ShouldPersistCard() {
      CardDto request = new CardDto(
              null,
              testUser.getId(),
              "1234567890123456",
              "John Doe",
              LocalDate.now().plusYears(2)
      );

      CardDto created = cardService.create(request);

      assertNotNull(created);
      assertNotNull(created.id());
      assertEquals("1234567890123456", created.number());
      assertEquals(testUser.getId(), created.userId());

      Optional<Card> maybe = cardRepository.findById(created.id());
      assertTrue(maybe.isPresent());
      assertEquals("1234567890123456", maybe.get().getNumber());
  }

  @Test
  void findById_ShouldReturnCard() {
    Card card = new Card();
    card.setUser(testUser);
    card.setNumber("1111222233334444");
    card.setHolder("Alice Cooper");
    card.setExpirationDate(LocalDate.now().plusYears(1));
    Card saved = cardRepository.save(card);

    CardDto dto = cardService.findById(saved.getId());
    assertNotNull(dto);
    assertEquals(saved.getId(), dto.id());
    assertEquals("1111222233334444", dto.number());
    assertEquals(testUser.getId(), dto.userId());
  }

  @Test
  void findByIds_ShouldReturnMultiple() {
    Card c1 = new Card();
    c1.setUser(testUser);
    c1.setNumber("1111222233334444");
    c1.setHolder("Holder1");
    c1.setExpirationDate(LocalDate.now().plusYears(1));

    Card c2 = new Card();
    c2.setUser(testUser);
    c2.setNumber("5555666677778888");
    c2.setHolder("Holder2");
    c2.setExpirationDate(LocalDate.now().plusYears(2));

    Card s1 = cardRepository.save(c1);
    Card s2 = cardRepository.save(c2);

    List<CardDto> dtos = cardService.findByIds(List.of(s1.getId(), s2.getId()));
    assertNotNull(dtos);
    assertEquals(2, dtos.size());

    Set<String> numbers = dtos.stream().map(CardDto::number).collect(Collectors.toSet());
    assertTrue(numbers.contains("1111222233334444"));
    assertTrue(numbers.contains("5555666677778888"));
  }

  @Test
  void findAll_ShouldReturnAll() {
    Card c1 = new Card();
    c1.setUser(testUser);
    c1.setNumber("1111222233334444");
    c1.setHolder("Holder1");
    c1.setExpirationDate(LocalDate.now().plusYears(1));

    Card c2 = new Card();
    c2.setUser(testUser);
    c2.setNumber("5555666677778888");
    c2.setHolder("Holder2");
    c2.setExpirationDate(LocalDate.now().plusYears(2));

    cardRepository.saveAll(List.of(c1, c2));

    List<CardDto> all = cardService.findAll();
    assertNotNull(all);
    assertTrue(all.size() >= 2);

    Set<String> numbers = all.stream().map(CardDto::number).collect(Collectors.toSet());
    assertTrue(numbers.contains("1111222233334444"));
    assertTrue(numbers.contains("5555666677778888"));
  }

  @Test
  void update_ShouldUpdateCard() {
    Card card = new Card();
    card.setUser(testUser);
    card.setNumber("1111222233334444");
    card.setHolder("Before");
    card.setExpirationDate(LocalDate.now().plusYears(1));
    Card saved = cardRepository.save(card);

    CardDto request =
        new CardDto(
            saved.getId(),
            testUser.getId(),
            saved.getNumber(),
            "After",
            LocalDate.now().plusYears(2));

    cardService.update(saved.getId(), request);

    Card updated = cardRepository.findById(saved.getId()).orElseThrow();
    assertEquals("After", updated.getHolder());
    assertEquals(LocalDate.now().plusYears(2), updated.getExpirationDate());
    assertEquals(testUser.getId(), updated.getUser().getId());
  }

  @Test
  void delete_ShouldRemoveCard() {
    Card card = new Card();
    card.setUser(testUser);
    card.setNumber("1111222233334444");
    card.setHolder("ToDelete");
    card.setExpirationDate(LocalDate.now().plusYears(1));
    Card saved = cardRepository.save(card);

    cardService.delete(saved.getId());

    assertFalse(cardRepository.findById(saved.getId()).isPresent());
  }
}

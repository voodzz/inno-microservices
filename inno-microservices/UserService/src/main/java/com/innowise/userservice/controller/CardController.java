package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.CardDto;
import com.innowise.userservice.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing cards.
 *
 * <p>Provides endpoints to create, retrieve, update, and delete {@link CardDto} entities. All
 * endpoints produce and consume JSON. Uses {@link CardService} for business logic.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/cards")
public class CardController {
  private final CardService service;

  /**
   * Creates a new card.
   *
   * @param request the card data to create
   * @return a {@link ResponseEntity} containing the created {@link CardDto} and HTTP status 201
   *     (Created)
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardDto request) {
    CardDto response = service.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieves a card by its unique identifier.
   *
   * @param id the ID of the card
   * @return a {@link ResponseEntity} containing the {@link CardDto} and HTTP status 200 (OK)
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  public ResponseEntity<CardDto> findById(@PathVariable Long id) {
    CardDto response = service.findById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Retrieves all cards or a filtered list by IDs.
   *
   * @param ids optional list of card IDs to filter by
   * @return a {@link ResponseEntity} containing the list of {@link CardDto} objects and HTTP status
   *     200 (OK), or HTTP status 204 (No Content) if no cards are found
   */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<CardDto>> findAll(@RequestParam(required = false) List<Long> ids) {
    List<CardDto> cards;
    if (ids == null) {
      cards = service.findAll();
    } else {
      cards = service.findByIds(ids);
    }
    if (cards.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(cards);
  }

  /**
   * Updates an existing card by its ID.
   *
   * @param id the ID of the card to update
   * @param request the updated card data
   * @return a {@link ResponseEntity} with HTTP status 200 (OK) if updated successfully
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  public ResponseEntity<Void> updateCard(
      @PathVariable Long id, @Valid @RequestBody CardDto request) {
    service.update(id, request);
    return ResponseEntity.ok().build();
  }

  /**
   * Deletes a card by its ID.
   *
   * @param id the ID of the card to delete
   * @return a {@link ResponseEntity} with HTTP status 204 (No Content) if deleted successfully
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}

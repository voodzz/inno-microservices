package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.CardDto;
import com.innowise.userservice.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/cards")
public class CardController {
  private final CardService service;

  @PostMapping
  public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardDto request) {
    CardDto response = service.createCard(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CardDto> findById(@PathVariable Long id) {
    CardDto response = service.findById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<CardDto>> findByIds(@RequestParam List<Long> ids) {
    List<CardDto> cards = service.findByIds(ids);
    if (cards.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(cards);
  }

  @PutMapping
  public ResponseEntity<Void> updateCard(@RequestParam Long id, @Valid @RequestBody CardDto request) {
    service.updateCard(id, request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
    service.deleteCard(id);
    return ResponseEntity.noContent().build();
  }
}

package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.CardResponse;
import com.innowise.userservice.model.dto.CreateCardRequest;
import com.innowise.userservice.model.dto.UpdateCardRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController("/api/v1/cards")
public class CardController {
  private final CardService service;

  @PostMapping("/create")
  public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest request) {
    CardResponse response = service.createCard(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CardResponse> findById(@PathVariable Long id) {
    CardResponse response = service.findById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/list")
  public ResponseEntity<List<CardResponse>> findByIds(@RequestParam List<Long> ids) {
    List<CardResponse> cards = service.findByIds(ids);
    if (cards.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(cards);
  }

  @PutMapping("/update")
  public ResponseEntity<Void> updateCard(@Valid @RequestBody UpdateCardRequest request) {
    service.updateCard(request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
    service.deleteCard(id);
    return ResponseEntity.noContent().build();
  }
}

package com.innowise.userservice.exception;

public class CardUpdateException extends RuntimeException {
  public CardUpdateException(Long id) {
    super("Update failed for card with id " + id);
  }
}

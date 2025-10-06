package com.innowise.userservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Entity representing a payment card in the system.
 *
 * <p>This entity is mapped to the {@code card_info} table and contains details such as card number,
 * holder name, expiration date, and the associated {@link User}.
 *
 * <p>Equality is based on the {@code number} field.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "number")
@ToString(exclude = "user")
@Table(name = "card_info")
@Entity
public class Card {

  /**
   * The unique identifier of the card. Generated automatically by the database using identity
   * strategy.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The user who owns the card.
   *
   * <p>Mapped as a many-to-one relationship with {@link User}. Fetched lazily by default.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  /** The card number. Must be unique and cannot be {@code null}. */
  @Column(nullable = false, unique = true)
  private String number;

  /** The name of the cardholder. Cannot be {@code null}. */
  @Column(nullable = false)
  private String holder;

  /** The expiration date of the card. Cannot be {@code null}. */
  @Column(name = "expiration_date", nullable = false)
  private LocalDate expirationDate;
}

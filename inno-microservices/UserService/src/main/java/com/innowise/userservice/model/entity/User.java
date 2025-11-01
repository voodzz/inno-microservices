package com.innowise.userservice.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a user in the system.
 *
 * <p>This entity is mapped to the {@code users} table and contains personal information such as
 * name, surname, birthdate, and email. A user can own multiple {@link Card} entities.
 *
 * <p>Equality is based on the {@code email} field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "email")
@ToString(exclude = "cards")
@Table(name = "users")
@Entity
public class User {

  /**
   * The unique identifier of the user. Generated automatically by the database using identity
   * strategy.
   */
  @Id
  private Long id;

  /** The user's first name. Cannot be {@code null}. */
  @Column(nullable = false)
  private String name;

  /** The user's last name. Cannot be {@code null}. */
  @Column(nullable = false)
  private String surname;

  /** The user's birthdate. Cannot be {@code null}. */
  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  /** The user's email address. Must be unique and cannot be {@code null}. */
  @Column(unique = true, nullable = false)
  private String email;

  /**
   * The list of cards owned by the user.
   *
   * <p>Mapped as a one-to-many relationship with {@link Card}. Uses cascade operations and orphan
   * removal.
   */
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
  private List<Card> cards = new ArrayList<>();
}

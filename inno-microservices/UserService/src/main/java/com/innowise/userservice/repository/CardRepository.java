package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Card} entities.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD operations and adds custom query
 * methods for card-specific lookups and updates.
 */
public interface CardRepository extends JpaRepository<Card, Long> {

  /**
   * Saves the given card entity.
   *
   * @param entity the card entity to save
   * @return the persisted card entity
   */
  Card save(Card entity);

  /**
   * Retrieves a card by its unique identifier.
   *
   * @param id the ID of the card
   * @return an {@link Optional} containing the card if found, otherwise empty
   */
  Optional<Card> findById(Long id);

  /**
   * Retrieves all cards whose IDs are in the given collection.
   *
   * <p>This method uses a JPQL query.
   *
   * @param ids the collection of card IDs
   * @return a list of cards matching the provided IDs
   */
  @Query("SELECT c FROM Card c WHERE c.id IN :ids")
  List<Card> findByIdIn(@Param("ids") Collection<Long> ids);

  /**
   * Retrieves all cards.
   *
   * @return a list of all cards in the database
   */
  List<Card> findAll();

  /**
   * Updates the user, holder, and expiration date of a card by its ID.
   *
   * <p>This method is executed as a native modifying SQL query inside a transaction.
   *
   * @param id the ID of the card to update
   * @param userId the ID of the user associated with the card
   * @param holder the new cardholder name
   * @param expirationDate the new expiration date of the card
   * @return the number of affected rows (should be {@code 1} if the card was updated)
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      value =
          """
             UPDATE card_info
             SET user_id = :user,
                 holder = :holder,
                 expiration_date = :date
             WHERE id = :id
             """,
      nativeQuery = true)
  @Transactional
  int updateById(
      @Param("id") Long id,
      @Param("user") Long userId,
      @Param("holder") String holder,
      @Param("date") LocalDate expirationDate);

  /**
   * Deletes the given card entity.
   *
   * @param entity the card entity to delete
   */
  void delete(Card entity);
}

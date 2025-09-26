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

public interface CardRepository extends JpaRepository<Card, Long> {

  Card save(Card entity);

  Optional<Card> findById(Long id);

  @Query("SELECT c FROM Card c WHERE c.id IN :ids")
  List<Card> findByIdIn(@Param("ids") Collection<Long> ids);

  List<Card> findAll();

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      value =
          """
             UPDATE card_info
             SET user_id = :user,
                 holder = "holder",
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

  void delete(Card entity);
}

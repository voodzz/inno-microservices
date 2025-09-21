package com.innowise.userservice.repository;

import com.innowise.userservice.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

  @Query("SELECT c FROM Card c WHERE c.id IN :ids")
  List<Card> findByIdIn(@Param("ids") Collection<Long> ids);

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
  int updateById(
      @Param("id") Long id,
      @Param("user") Long userId,
      @Param("holder") String holder,
      @Param("date") LocalDate expirationDate);
}

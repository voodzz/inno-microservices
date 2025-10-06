package com.innowise.authservice.repository;

import com.innowise.authservice.model.entity.RefreshToken;
import com.innowise.authservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  @Query("SELECT t FROM RefreshToken t WHERE t.token = :token")
  Optional<RefreshToken> findByToken(@Param("token") String token);

  @Query("SELECT t FROM RefreshToken t WHERE t.user = :user")
  Optional<RefreshToken> findByUser(@Param("user") User user);

  @Query("SELECT t FROM RefreshToken t WHERE t.user.id = :id")
  Optional<RefreshToken> findByUserId(@Param("id") Long userId);

  @Transactional
  @Modifying
  @Query("DELETE FROM RefreshToken t WHERE t.expiryDate < :now")
  void deleteAllExpiredSince(@Param("now") Instant now);

  @Transactional
  @Modifying
  @Query("DELETE FROM RefreshToken t WHERE t.user = :user")
  void deleteByUser(@Param("user") User user);

  @Transactional
  @Modifying
  @Query("DELETE FROM RefreshToken t WHERE t.user.id = :id")
  void deleteByUserId(@Param("user") Long id);
}

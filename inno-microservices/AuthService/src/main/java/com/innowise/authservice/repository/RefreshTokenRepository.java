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

/**
 * Repository interface for performing CRUD and custom operations on {@link RefreshToken} entities.
 *
 * <p>This interface extends {@link JpaRepository} to provide built-in persistence methods and
 * defines additional queries for managing refresh tokens in an authentication system.
 *
 * <p>Typical use cases include retrieving, deleting, and cleaning up expired refresh tokens
 * associated with {@link User} entities.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  /**
   * Finds a refresh token entity by its token string value.
   *
   * @param token the refresh token string to search for
   * @return an {@link Optional} containing the matching {@link RefreshToken}, or empty if not found
   */
  @Query("SELECT t FROM RefreshToken t WHERE t.token = :token")
  Optional<RefreshToken> findByToken(@Param("token") String token);

  /**
   * Finds a refresh token associated with the specified user.
   *
   * @param user the {@link User} whose refresh token should be retrieved
   * @return an {@link Optional} containing the user's {@link RefreshToken}, or empty if not found
   */
  @Query("SELECT t FROM RefreshToken t WHERE t.user = :user")
  Optional<RefreshToken> findByUser(@Param("user") User user);

  /**
   * Finds a refresh token associated with a user by their unique identifier.
   *
   * @param userId the ID of the {@link User}
   * @return an {@link Optional} containing the user's {@link RefreshToken}, or empty if not found
   */
  @Query("SELECT t FROM RefreshToken t WHERE t.user.id = :id")
  Optional<RefreshToken> findByUserId(@Param("id") Long userId);

  /**
   * Deletes all refresh tokens that have expired before the specified timestamp.
   *
   * <p>This operation is transactional and modifies the database state.
   *
   * @param now the timestamp representing the current time; tokens with an expiry date before this
   *     time will be deleted
   */
  @Transactional
  @Modifying
  @Query("DELETE FROM RefreshToken t WHERE t.expiryDate < :now")
  void deleteAllExpiredSince(@Param("now") Instant now);

  /**
   * Deletes the refresh token associated with the specified user.
   *
   * <p>This operation is transactional and modifies the database state.
   *
   * @param user the {@link User} whose refresh token should be deleted
   */
  @Transactional
  @Modifying
  @Query("DELETE FROM RefreshToken t WHERE t.user = :user")
  void deleteByUser(@Param("user") User user);

  /**
   * Deletes the refresh token associated with the user identified by the given ID.
   *
   * <p>This operation is transactional and modifies the database state.
   *
   * @param id the ID of the {@link User} whose refresh token should be deleted
   */
  @Transactional
  @Modifying
  @Query("DELETE FROM RefreshToken t WHERE t.user.id = :id")
  void deleteByUserId(@Param("user") Long id);
}

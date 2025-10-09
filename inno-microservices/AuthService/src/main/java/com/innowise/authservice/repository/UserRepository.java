package com.innowise.authservice.repository;

import com.innowise.authservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository interface for accessing and managing {@link User} entities.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD operations and defines additional query
 * methods for retrieving users by their username and checking for username existence.
 *
 * <p>This repository is typically used by authentication and user management services to look up
 * users and validate registration constraints.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Retrieves a user entity by its unique username.
   *
   * @param username the username of the {@link User} to find
   * @return an {@link Optional} containing the matching {@link User}, or empty if no user is found
   */
  @Query("SELECT u FROM User u WHERE u.username = :username")
  Optional<User> findByUsername(@Param("username") String username);

  /**
   * Checks whether a user with the specified username already exists.
   *
   * <p>This method is useful for enforcing unique username constraints during registration.
   *
   * @param username the username to check for existence
   * @return {@code true} if a user with the given username exists; {@code false} otherwise
   */
  boolean existsByUsername(String username);
}

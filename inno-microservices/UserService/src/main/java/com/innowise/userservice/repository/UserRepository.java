package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.User;
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
 * Repository interface for managing {@link User} entities.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD operations and adds custom query
 * methods for user-specific lookups and updates.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Saves the given user entity.
   *
   * @param save the user entity to save
   * @return the persisted user entity
   */
  User save(User save);

  /**
   * Retrieves a user by their unique identifier.
   *
   * @param aLong the ID of the user
   * @return an {@link Optional} containing the user if found, otherwise empty
   */
  Optional<User> findById(Long aLong);

  /**
   * Checks if a user with the specified email exists.
   *
   * @param email the email address to check
   * @return {@code true} if a user with the given email exists, otherwise {@code false}
   */
  boolean existsByEmail(String email);

  /**
   * Retrieves all users whose IDs are in the given collection.
   *
   * <p>This method uses a native SQL query.
   *
   * @param ids the collection of user IDs
   * @return a list of users matching the provided IDs
   */
  @Query(value = "SELECT u.* FROM users u WHERE u.id IN :ids", nativeQuery = true)
  List<User> findByIdIn(@Param("ids") Collection<Long> ids);

  /**
   * Retrieves a user by their email address.
   *
   * <p>This method uses a JPQL query.
   *
   * @param email the email address of the user
   * @return an {@link Optional} containing the user if found, otherwise empty
   */
  @Query("SELECT u FROM User u WHERE u.email = :email")
  Optional<User> findByEmail(@Param("email") String email);

  /**
   * Retrieves all users.
   *
   * @return a list of all users in the database
   */
  List<User> findAll();

  /**
   * Updates the name, surname, and birthdate of a user by their ID.
   *
   * <p>This method is executed as a modifying JPQL query inside a transaction.
   *
   * @param id the ID of the user to update
   * @param name the new name of the user
   * @param surname the new surname of the user
   * @param birthDate the new birthdate of the user
   * @return the number of affected rows (should be {@code 1} if the user was updated)
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
        UPDATE User u
        SET u.name = :name,
            u.surname = :surname,
            u.birthDate = :birthDate
        WHERE u.id = :id
        """)
  @Transactional
  int updateById(
      @Param("id") Long id,
      @Param("name") String name,
      @Param("surname") String surname,
      @Param("birthDate") LocalDate birthDate);

  /**
   * Deletes the given user entity.
   *
   * @param entity the user entity to delete
   */
  void delete(User entity);
}

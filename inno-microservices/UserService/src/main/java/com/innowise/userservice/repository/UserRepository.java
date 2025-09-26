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

public interface UserRepository extends JpaRepository<User, Long> {

  User save(User save);

  Optional<User> findById(Long aLong);

  @Query(value = "SELECT u.* FROM users u WHERE u.id IN :ids", nativeQuery = true)
  List<User> findByIdIn(@Param("ids") Collection<Long> ids);

  @Query("SELECT u FROM User u WHERE u.email = :email")
  Optional<User> findByEmail(@Param("email") String email);

  List<User> findAll();

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

  void delete(User entity);
}

package com.innowise.authservice.repository;

import com.innowise.authservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u WHERE u.username = :username")
  Optional<User> findByUsername(@Param("username") String username);

  boolean existsByUsername(String username);
}

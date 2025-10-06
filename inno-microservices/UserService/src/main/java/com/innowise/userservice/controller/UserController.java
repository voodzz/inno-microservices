package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing users.
 *
 * <p>Provides endpoints to create, retrieve, update, and delete {@link UserDto} entities. Supports
 * optional filtering by IDs or email. All endpoints produce and consume JSON. Uses {@link
 * UserService} for business logic.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/users")
public class UserController {
  private final UserService service;

  /**
   * Creates a new user.
   *
   * @param request the user data to create
   * @return a {@link ResponseEntity} containing the created {@link UserDto} and HTTP status 201
   *     (Created)
   */
  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto request) {
    UserDto response = service.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieves a user by their unique identifier.
   *
   * @param id the ID of the user
   * @return a {@link ResponseEntity} containing the {@link UserDto} and HTTP status 200 (OK)
   */
  @GetMapping("/{id}")
  public ResponseEntity<UserDto> findById(@PathVariable Long id) {
    UserDto response = service.findById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Retrieves users with optional filtering.
   *
   * <p>Supports the following filters via the "filter" request parameter:
   *
   * <ul>
   *   <li>{@code ids}: retrieves users by a list of IDs
   *   <li>{@code email}: retrieves a user by email
   *   <li>default or null: retrieves all users
   * </ul>
   *
   * @param filter optional filter type ("ids" or "email")
   * @param ids optional list of user IDs (used when filter="ids")
   * @param email optional email address (used when filter="email")
   * @return a {@link ResponseEntity} containing a list of {@link UserDto} objects with status 200
   *     (OK), or 204 (No Content) if no users are found
   */
  @GetMapping
  public ResponseEntity<List<UserDto>> findBy(
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) List<Long> ids,
      @RequestParam(required = false) String email) {
    if (filter == null) {
      List<UserDto> all = service.findAll();
      return getListResponseEntity(all);
    }
    return switch (filter) {
      case "ids" -> {
        List<UserDto> responseList = service.findByIds(ids);
        yield getListResponseEntity(responseList);
      }
      case "email" -> {
        UserDto user = service.findByEmail(email);
        yield ResponseEntity.ok(List.of(user));
      }
      default -> {
        List<UserDto> all = service.findAll();
        yield getListResponseEntity(all);
      }
    };
  }

  /**
   * Helper method to create a {@link ResponseEntity} for a list of users.
   *
   * @param all the list of users
   * @return {@link ResponseEntity} with HTTP status 200 (OK) if list is not empty, or 204 (No
   *     Content) if the list is empty
   */
  private ResponseEntity<List<UserDto>> getListResponseEntity(List<UserDto> all) {
    if (all.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(all);
  }

  /**
   * Updates an existing user by their ID.
   *
   * @param id the ID of the user to update
   * @param request the updated user data
   * @return a {@link ResponseEntity} with HTTP status 200 (OK) if updated successfully
   */
  @PutMapping("/{id}")
  public ResponseEntity<Void> updateUser(
      @PathVariable Long id, @Valid @RequestBody UserDto request) {
    service.update(id, request);
    return ResponseEntity.ok().build();
  }

  /**
   * Deletes a user by their ID.
   *
   * @param id the ID of the user to delete
   * @return a {@link ResponseEntity} with HTTP status 204 (No Content) if deleted successfully
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}

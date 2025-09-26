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

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/users")
public class UserController {
  private final UserService service;

  @PostMapping
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto request) {
    UserDto response = service.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> findById(@PathVariable Long id) {
    UserDto response = service.findById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> findBy(
      @RequestParam String filter,
      @RequestParam(required = false) List<Long> ids,
      @RequestParam(required = false) String email) {
    switch (filter) {
      case "ids":
        List<UserDto> responseList = service.findByIds(ids);
        if (responseList.isEmpty()) {
          return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(responseList);
      case "email":
        UserDto user = service.findByEmail(email);
        return ResponseEntity.ok(List.of(user));
      default:
        return ResponseEntity.ok(service.findAll());
    }
  }

  @PutMapping
  public ResponseEntity<Void> updateUser(@RequestParam Long id, @Valid @RequestBody UserDto request) {
    service.updateUser(id, request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    service.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}

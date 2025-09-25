package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.CreateUserRequest;
import com.innowise.userservice.model.dto.UpdateUserRequest;
import com.innowise.userservice.model.dto.UserResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController("api/v1/users")
public class UserController {
  private final UserService service;

  @PostMapping("/create")
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    UserResponse response = service.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
    UserResponse response = service.findById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/list")
  public ResponseEntity<List<UserResponse>> findByIds(@RequestParam List<Long> ids) {
    List<UserResponse> responseList = service.findByIds(ids);
    if (responseList.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(responseList);
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<UserResponse> findByEmail(@PathVariable String email) {
    UserResponse response = service.findByEmail(email);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/update")
  public ResponseEntity<Void> updateUser(@Valid @RequestBody UpdateUserRequest request) {
    service.updateUser(request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    service.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}

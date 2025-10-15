package com.innowise.orderservice.client;

import com.innowise.orderservice.model.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Declarative REST client for inter-service communication with the User Service.
 *
 * <p>It is configured using Spring Cloud OpenFeign to retrieve user details necessary for enriching
 * the Order DTOs (OrderUserDto) during a request lifecycle in the Order Service. * The base URL for
 * this client is defined by the property {@code userservice.url}.
 */
@FeignClient(name = "userservice", url = "${userservice.url}")
public interface UserServiceClient {

  /**
   * Retrieves the core details of a user from the User Service using their email address.
   *
   * <p>This method performs a synchronous GET request to the User Service's user search endpoint.
   * The email is passed as a query parameter.
   *
   * @param email The unique email address of the user.
   * @return The {@link UserDto} containing the user's ID, name, surname, birthdate and email.
   * @throws feign.FeignException if the remote service returns an HTTP error (e.g., 404 if the user
   *     is not found, 500 for internal errors).
   */
  @GetMapping("/api/v1/users")
  UserDto getUserByEmail(@RequestParam String email);
}

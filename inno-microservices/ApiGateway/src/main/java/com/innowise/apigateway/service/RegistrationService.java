package com.innowise.apigateway.service;

import com.innowise.apigateway.dto.AuthRequest;
import com.innowise.apigateway.dto.AuthResponse;
import com.innowise.apigateway.dto.RegistrationRequest;
import com.innowise.apigateway.dto.UserServiceResponse;
import com.innowise.apigateway.exception.TransactionFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service class responsible for coordinating the registration of a new user across multiple
 * microservices (User Service and Auth Service) in a transactional manner.
 * It uses {@link WebClient} for non-blocking communication.
 */
@Service
public class RegistrationService {
  private final WebClient webClient;
  private final String userServiceUrl;
  private final String authServiceUrl;
  private final String internalSecret;

  /**
   * Constructs the RegistrationService.
   *
   * @param webClientBuilder Builder to create the reactive WebClient.
   * @param userServiceUrl The base URL for the User Service.
   * @param authServiceUrl The base URL for the Auth Service.
   * @param internalSecret The secret key used for internal service-to-service communication.
   */
  public RegistrationService(
      WebClient.Builder webClientBuilder,
      @Value("${user.service.url}") String userServiceUrl,
      @Value("${auth.service.url}") String authServiceUrl,
      @Value("${security.jwt.secret-key}") String internalSecret) {
    this.webClient = webClientBuilder.build();
    this.userServiceUrl = userServiceUrl;
    this.authServiceUrl = authServiceUrl;
    this.internalSecret = internalSecret;
  }

  /**
   * Registers a new user by calling the User Service, then the Auth Service. If the Auth Service
   * call fails, it attempts to roll back the user creation in the User Service and throws a
   * {@link TransactionFailedException}.
   *
   * @param request The {@link RegistrationRequest} DTO.
   * @return A Mono emitting the {@link AuthResponse} containing the JWT token.
   * @throws TransactionFailedException if any microservice call fails, leading to a rollback.
   */
  public Mono<AuthResponse> registerUser(RegistrationRequest request) {
    UserServiceResponse userServiceRequest =
        new UserServiceResponse(
            null,
            request.name(),
            request.surname(),
            request.birthDate(),
            request.email(),
            request.cards());
    AuthRequest authRequest = new AuthRequest(request.email(), request.password());
    return createUserInUserService(userServiceRequest)
        .flatMap(
            response ->
                webClient
                    .post()
                    .uri(authServiceUrl + "/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(authRequest)
                    .retrieve()
                    .bodyToMono(AuthResponse.class)
                    .onErrorResume(
                        error ->
                            rollbackUserService(response.id())
                                .then(
                                    Mono.error(
                                        new TransactionFailedException(
                                            "AuthService registration failed. UserService rolled back.")))));
  }

  /**
   * Communicates with the User Service to create the new user record.
   * This is the first step of the distributed transaction.
   *
   * @param request The DTO containing the user data for the User Service.
   * @return A Mono emitting the {@link UserServiceResponse} with the created user's data.
   * @throws TransactionFailedException if the User Service call fails.
   */
  private Mono<UserServiceResponse> createUserInUserService(UserServiceResponse request) {
    return webClient
        .post()
        .uri(userServiceUrl + "/api/v1/users")
        .header("X-Internal-Secret", internalSecret)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(UserServiceResponse.class)
        .onErrorResume(
            error ->
                Mono.error(
                    new TransactionFailedException(
                        "Failed to create user in UserService: " + error.getMessage())));
  }

  /**
   * Executes the rollback by sending a DELETE request to the User Service to remove the created user.
   * This is called if a subsequent service (like Auth Service) fails.
   *
   * @param id The ID of the user to delete/rollback.
   * @return A {@code Mono<Void>} that completes after the rollback attempt.
   */
  private Mono<Void> rollbackUserService(Long id) {
    return webClient
        .delete()
        .uri(userServiceUrl + "/api/v1/users/" + id)
        .header("X-Internal-Secret", internalSecret)
        .retrieve()
        .toBodilessEntity()
        .then();
  }
}

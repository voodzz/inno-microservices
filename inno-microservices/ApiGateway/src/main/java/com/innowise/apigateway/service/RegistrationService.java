package com.innowise.apigateway.service;

import com.innowise.apigateway.dto.AuthRequest;
import com.innowise.apigateway.dto.AuthResponse;
import com.innowise.apigateway.dto.UserDto;
import com.innowise.apigateway.dto.UserServiceDto;
import com.innowise.apigateway.exception.TransactionFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RegistrationService {
  private final WebClient webClient;
  private final String userServiceUrl;
  private final String authServiceUrl;
  private final String internalSecret;

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

  public Mono<AuthResponse> registerUser(UserDto request) {
    UserServiceDto userServiceRequest =
        new UserServiceDto(
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

  private Mono<UserServiceDto> createUserInUserService(UserServiceDto request) {
    return webClient
        .post()
        .uri(userServiceUrl + "/api/v1/users")
        .header("X-Internal-Secret", internalSecret)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(UserServiceDto.class)
        .onErrorResume(
            error ->
                Mono.error(
                    new TransactionFailedException(
                        "Failed to create user in UserService: " + error.getMessage())));
  }

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

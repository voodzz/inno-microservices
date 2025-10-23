package com.innowise.authservice.service;

import com.innowise.authservice.exception.AlreadyExistsException;
import com.innowise.authservice.model.dto.AuthRequest;
import com.innowise.authservice.model.dto.RegisterUserRequest;
import com.innowise.authservice.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedRegistrationService {
  private final AuthService authService;
  private final WebClient.Builder webClientBuilder;

  @Value("${userservice.url}")
  private String userServiceUrl;

  @Value("${security.jwt.secret-key}")
  private String jwtSecretKey;

  public Mono<UserDto> registerUser(RegisterUserRequest request) {
    return createUser(request)
        .flatMap(
            userDto ->
                createAuthCredentials(request)
                    .thenReturn(userDto)
                    .onErrorResume(e -> rollback(userDto.id()).then(Mono.error(e))))
        .doOnError(e -> log.error("Registration failed for user {}", request.email(), e));
  }

  private Mono<UserDto> createUser(RegisterUserRequest request) {
    UserDto userDto =
        UserDto.builder()
            .name(request.name())
            .surname(request.surname())
            .birthDate(request.birthDate())
            .email(request.email())
            .build();

    WebClient webClient = webClientBuilder.build();

    return webClient
        .post()
        .uri(userServiceUrl + "/api/v1/users")
        .header("X-Internal-Secret", jwtSecretKey)
        .bodyValue(userDto)
        .retrieve()
        .onStatus(
            HttpStatus.CONFLICT::equals,
            response ->
                Mono.error(
                    new AlreadyExistsException(
                        "User with email '%s' already exists".formatted(request.email()))))
        .bodyToMono(UserDto.class)
        .timeout(Duration.ofSeconds(10));
  }

  private Mono<Void> createAuthCredentials(RegisterUserRequest request) {
    return Mono.fromRunnable(
            () -> authService.register(new AuthRequest(request.email(), request.password())))
        .subscribeOn(boundedElastic())
        .onErrorMap(
            AlreadyExistsException.class,
            e -> new AlreadyExistsException("User '%s' already exists".formatted(request.email())))
        .doOnSuccess(v -> log.info("Auth credentials created successfully for {}", request.email()))
        .doOnError(e -> log.error("Failed to create auth credentials for {}", request.email(), e))
        .then();
  }

  private Mono<Void> rollback(Long userId) {
    WebClient webClient = webClientBuilder.build();

    return webClient
        .delete()
        .uri(userServiceUrl + "/api/v1/users/%d".formatted(userId))
        .header("X-Internal-Secret", jwtSecretKey)
        .retrieve()
        .toBodilessEntity()
        .timeout(Duration.ofSeconds(10))
        .retryWhen(Retry.backoff(3, Duration.ofMillis(200)))
        .doOnSuccess(v -> log.info("Rolled back user profile with ID: {}", userId))
        .doOnError(e -> log.error("Failed to rollback user profile with ID: {}", userId, e))
        .then();
  }
}

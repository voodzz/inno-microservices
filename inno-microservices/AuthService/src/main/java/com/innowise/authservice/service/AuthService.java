package com.innowise.authservice.service;

import com.innowise.authservice.exception.AlreadyExistsException;
import com.innowise.authservice.exception.TransactionFailedException;
import com.innowise.authservice.model.RoleEnum;
import com.innowise.authservice.model.dto.AuthRequest;
import com.innowise.authservice.model.dto.AuthResponse;
import com.innowise.authservice.model.dto.RegistrationRequest;
import com.innowise.authservice.model.dto.UserServiceDto;
import com.innowise.authservice.model.entity.RefreshToken;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashSet;

/**
 * Service handling user authentication, registration, and token management.
 *
 * <p>Integrates with JWT and refresh token services to issue and renew tokens.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
  private final WebClient webClient = WebClient.builder().build();
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;

  @Value("${user.service.url}")
  private String userServiceUrl;

  @Value("${security.jwt.secret-key}")
  private String internalSecret;

  /**
   * Authenticates a user and generates new access and refresh tokens.
   *
   * @param request the login request containing username and password
   * @return an {@link AuthResponse} with generated tokens
   */
  public AuthResponse login(AuthRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));

    User user = (User) authentication.getPrincipal();

    String accessToken = jwtService.generateAccessToken(user);
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

    return new AuthResponse(accessToken, refreshToken.getToken());
  }

  /**
   * Refreshes the access token using a valid refresh token.
   *
   * @param refreshTokenString the refresh token string
   * @return an {@link AuthResponse} containing new access and refresh tokens
   */
  public AuthResponse refresh(String refreshTokenString) {
    RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
    refreshToken = refreshTokenService.verifyExpiration(refreshToken);

    User user = refreshToken.getUser();
    String accessToken = jwtService.generateAccessToken(user);
    refreshToken = refreshTokenService.createRefreshToken(user);

    return new AuthResponse(accessToken, refreshToken.getToken());
  }

  /**
   * Registers a new user.
   *
   * <p>Saves credentials in AuthService and user data in UserService. Roolls back
   * AuthService on UserService failure.
   *
   * @param request the registration request
   * @throws TransactionFailedException if the AuthService or UserService fails
   * @throws AlreadyExistsException if the user already exists in AuthService
   */
  @Transactional(rollbackFor = {TransactionFailedException.class})
  public void register(RegistrationRequest request) {
    User registered = registerInAuthService(request.email(), request.password());
    UserServiceDto userServiceRequest =
        UserServiceDto.builder()
            .id(registered.getId())
            .name(request.name())
            .surname(request.surname())
            .birthDate(request.birthDate())
            .email(request.email())
            .cards(request.cards())
            .build();
    createUserInUserService(userServiceRequest);
  }

  /**
   * Registers a new user with an encoded password and a default {@code USER} role.
   *
   * @param username the username of the user
   * @param password the password of the user
   * @throws AlreadyExistsException if a user with the same username already exists
   */
  private User registerInAuthService(String username, String password) {
    if (userRepository.existsByUsername(username)) {
      throw new AlreadyExistsException("User '%s' already exisits".formatted(username));
    }

    User user = new User(null, username, passwordEncoder.encode(password), new HashSet<>());
    user.getRoles().add(new Role(null, RoleEnum.ROLE_USER, user));

    return userRepository.save(user);
  }

  /**
   * Communicates with the User Service to create the new user record. This is the second step of the
   * distributed transaction.
   *
   * @param request The DTO containing the user data for the User Service.
   * @throws TransactionFailedException if the User Service call fails.
   */
  private void createUserInUserService(UserServiceDto request) {
    try {
      webClient
          .post()
          .uri(userServiceUrl + "/api/v1/users")
          .header("X-Internal-Secret", internalSecret)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .retrieve()
          .onStatus(
              HttpStatusCode::isError,
              r ->
                  Mono.error(
                      new TransactionFailedException(
                          "Creation of user in UserService failed with status code "
                              + r.statusCode())))
          .toBodilessEntity()
          .block();
    } catch (RuntimeException ex) {
      throw new TransactionFailedException("UserService registration failed. AuthService rolled back", ex);
    }
  }
}

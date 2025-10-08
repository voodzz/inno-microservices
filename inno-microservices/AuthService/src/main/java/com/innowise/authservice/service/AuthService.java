package com.innowise.authservice.service;

import com.innowise.authservice.exception.AlreadyExistsException;
import com.innowise.authservice.model.RoleEnum;
import com.innowise.authservice.model.dto.AuthRequest;
import com.innowise.authservice.model.dto.AuthResponse;
import com.innowise.authservice.model.entity.RefreshToken;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;

  public User register(AuthRequest request) {
    if (userRepository.existsByUsername(request.username())) {
      throw new AlreadyExistsException("User '%s' already exisits".formatted(request.username()));
    }

    User user =
        new User(
            null, request.username(), passwordEncoder.encode(request.password()), new HashSet<>());
    user.getRoles().add(new Role(null, RoleEnum.USER, user));

    return userRepository.save(user);
  }

  public AuthResponse login(AuthRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));

    User user = (User) authentication.getPrincipal();

    String accessToken = jwtService.generateAccessToken(user);
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

    return new AuthResponse(accessToken, refreshToken.getToken());
  }

  public AuthResponse refresh(String refreshTokenString) {
      RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
      refreshToken = refreshTokenService.verifyExpiration(refreshToken);

      User user = refreshToken.getUser();
      String accessToken = jwtService.generateAccessToken(user);
      refreshToken = refreshTokenService.createRefreshToken(user);

      return new AuthResponse(accessToken, refreshToken.getToken());
  }
}

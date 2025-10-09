package com.innowise.authservice.service;

import com.innowise.authservice.exception.NotFoundException;
import com.innowise.authservice.exception.TokenExpiredException;
import com.innowise.authservice.model.entity.RefreshToken;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service for managing refresh tokens, including creation, retrieval, and expiration verification.
 *
 * <p>Integrates with {@link JwtService} to generate refresh tokens and persists them using {@link
 * RefreshTokenRepository}.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository tokenRepository;
  private final JwtService jwtService;

  /**
   * Creates a new refresh token for the given user.
   *
   * <p>If a refresh token already exists for the user, it is deleted before creating a new one.
   *
   * @param user the user for whom to create the refresh token
   * @return the newly created {@link RefreshToken}
   */
  public RefreshToken createRefreshToken(User user) {
    if (tokenRepository.findByUser(user).isPresent()) {
      tokenRepository.deleteByUser(user);
    }

    String token = jwtService.generateRefreshToken(user);
    RefreshToken refreshToken =
        new RefreshToken(
            null, token, user, Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()));

    return tokenRepository.save(refreshToken);
  }

  /**
   * Finds a refresh token by its token string.
   *
   * @param token the token string to search for
   * @return the corresponding {@link RefreshToken}
   * @throws NotFoundException if no token is found
   */
  public RefreshToken findByToken(String token) {
    return tokenRepository
        .findByToken(token)
        .orElseThrow(() -> new NotFoundException("RefreshToken with such token not found"));
  }

  /**
   * Verifies whether a refresh token has expired.
   *
   * <p>If the token is expired, it is deleted from the repository and a
   * {@link TokenExpiredException} is thrown.
   *
   * @param token the refresh token to verify
   * @return the same {@link RefreshToken} if it is valid
   * @throws TokenExpiredException if the token has expired
   */
  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().isBefore(Instant.now())) {
      tokenRepository.deleteById(token.getId());
      throw new TokenExpiredException("Token is expired. Make a new signin request.");
    }
    return token;
  }
}

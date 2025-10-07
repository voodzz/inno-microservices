package com.innowise.authservice.service;

import com.innowise.authservice.exception.NotFoundException;
import com.innowise.authservice.exception.TokenExpiredException;
import com.innowise.authservice.model.entity.RefreshToken;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository tokenRepository;
  private final JwtService jwtService;

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

  public RefreshToken findByToken(String token) {
    return tokenRepository
        .findByToken(token)
        .orElseThrow(() -> new NotFoundException("RefreshToken with such token not found"));
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().isBefore(Instant.now())) {
      tokenRepository.deleteById(token.getId());
      throw new TokenExpiredException("Token is expired. Make a new signin request.");
    }
    return token;
  }
}

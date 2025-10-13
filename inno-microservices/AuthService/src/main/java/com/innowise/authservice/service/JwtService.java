package com.innowise.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for generating, validating, and extracting information from JWT tokens.
 *
 * <p>Supports both access and refresh tokens with configurable expiration times and a secret key
 * defined in application properties.
 */
@Service
public class JwtService {
  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.access-expiration-time}")
  private Long accessTokenExpiration;

  @Getter
  @Value("${security.jwt.refresh-expiration-time}")
  private Long refreshTokenExpiration;

  /**
   * Generates a JWT access token for the given user, including roles as claims.
   *
   * @param userDetails the authenticated user details
   * @return a signed JWT access token
   */
  public String generateAccessToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(
        "roles",
        userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
    return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
  }

  /**
   * Generates a JWT refresh token for the given user without additional claims.
   *
   * @param userDetails the authenticated user details
   * @return a signed JWT refresh token
   */
  public String generateRefreshToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
  }

  /**
   * Creates a signed JWT token with the given claims, subject, and expiration time.
   *
   * @param claims the JWT claims to include
   * @param subject the token subject (usually the username)
   * @param expiration the expiration time in milliseconds
   * @return a signed JWT token string
   */
  private String createToken(Map<String, Object> claims, String subject, Long expiration) {
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Validates a JWT token against the given user details.
   *
   * @param token the JWT token string
   * @param userDetails the user details to validate against
   * @return {@code true} if the token is valid and not expired, {@code false} otherwise
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  /**
   * Checks whether a JWT token is expired.
   *
   * @param token the JWT token string
   * @return {@code true} if the token is expired, {@code false} otherwise
   */
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Extracts the username (subject) from a JWT token.
   *
   * @param token the JWT token string
   * @return the username contained in the token
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the expiration date from a JWT token.
   *
   * @param token the JWT token string
   * @return the expiration {@link Date} of the token
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extracts a specific claim from the token using a claim resolver function.
   *
   * @param token the JWT token string
   * @param claimResolver function to retrieve a specific claim
   * @param <T> type of the claim
   * @return the value of the claim
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
    Claims claims = extractAllClaims(token);
    return claimResolver.apply(claims);
  }

  /**
   * Extracts all claims from a JWT token.
   *
   * @param token the JWT token string
   * @return all claims contained in the token
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
  }

  /**
   * Returns the signing key used to sign and verify JWT tokens.
   *
   * @return the {@link SecretKey} derived from the secret key string
   */
  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}

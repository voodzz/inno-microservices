package com.innowise.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Service class for handling JWT token operations, such as validation and claim extraction. It uses
 * a secret key configured in application properties.
 */
@Service
public class JwtService {
  @Value("${security.jwt.secret-key}")
  private String secretKey;

  /**
   * Checks if a given JWT token is valid (i.e., not expired).
   *
   * @param token The JWT token.
   * @return true if the token is valid, false otherwise.
   */
  public boolean isTokenValid(String token) {
    return !isTokenExpired(token);
  }

  /**
   * Checks if a given JWT token has expired.
   *
   * @param token The JWT token.
   * @return true if the token is expired, false otherwise.
   */
  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Extracts the expiration date from a JWT token.
   *
   * @param token The JWT token.
   * @return The expiration {@link Date}.
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extracts a specific claim from the JWT's payload using a resolver function.
   *
   * @param token The JWT token.
   * @param claimResolver A function to resolve the desired claim from the {@link Claims}.
   * @param <T> The type of the claim to be extracted.
   * @return The extracted claim value.
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
    Claims claims = extractAllClaims(token);
    return claimResolver.apply(claims);
  }

  /**
   * Parses the JWT, verifies the signature, and extracts all claims from the token's payload.
   *
   * @param token The JWT string.
   * @return The {@link Claims} object containing all claims.
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Generates the signing key from the base64 encoded secret key configured in properties.
   *
   * @return The {@link SecretKey} used for signing and verification.
   */
  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }
}

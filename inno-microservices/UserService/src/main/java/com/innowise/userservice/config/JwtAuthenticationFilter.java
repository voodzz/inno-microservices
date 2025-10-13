package com.innowise.userservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * {@code JwtAuthenticationFilter} is a custom security filter that intercepts each HTTP request to
 * perform JWT-based authentication.
 *
 * <p>This filter extracts a JWT token from the {@code Authorization} header, validates it, and sets
 * the corresponding {@link Authentication} object in the {@link SecurityContextHolder}. If the
 * token is invalid or missing, the filter simply passes the request along the chain.
 *
 * <p>Exceptions during JWT processing are delegated to a configured
 * {@link HandlerExceptionResolver}.
 *
 * <p>This filter runs once per request (extends {@link OncePerRequestFilter}) and does not require
 * explicit registration when annotated with {@link org.springframework.stereotype.Component}.
 *
 * <p>Expected header format: {@code Authorization: Bearer <token>}.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String HEADER_NAME = "Authorization";
  private static final String HEADER_START = "Bearer ";
  private static final Integer BEARER_END = 7;

  private final HandlerExceptionResolver handlerExceptionResolver;

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    final String header = request.getHeader(HEADER_NAME);

    if (header == null || !header.startsWith(HEADER_START)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String jwt = header.substring(BEARER_END);
      final String username = extractUsername(jwt);

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (username != null && isTokenValid(jwt) && authentication == null) {
        List<String> roles = extractRoles(jwt);

        List<SimpleGrantedAuthority> authorities =
            roles.stream().map(SimpleGrantedAuthority::new).toList();

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(username, null, authorities);

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
    } catch (Exception ex) {
      handlerExceptionResolver.resolveException(request, response, null, ex);
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Validates whether the provided JWT token is still valid (i.e., not expired).
   *
   * @param token the JWT token
   * @return {@code true} if the token is valid; {@code false} otherwise
   */
  private boolean isTokenValid(String token) {
    return !isTokenExpired(token);
  }
  /**
   * Checks whether the JWT token has expired.
   *
   * @param token the JWT token
   * @return {@code true} if the token has expired; {@code false} otherwise
   */
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Extracts user roles from the JWT token claims.
   *
   * @param token the JWT token
   * @return a list of roles associated with the token, or an empty list if none are found
   */
  private List<String> extractRoles(String token) {
    List<?> roles = extractClaim(token, claims -> claims.get("roles", List.class));

    if (roles == null || roles.isEmpty()) {
      return List.of();
    }

    return roles.stream().map(Object::toString).toList();
  }

  /**
   * Extracts the username (subject) from the JWT token.
   *
   * @param token the JWT token
   * @return the username stored in the token
   */
  private String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the expiration date from the JWT token.
   *
   * @param token the JWT token
   * @return the token's expiration date
   */
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extracts a specific claim from the JWT token using the provided resolver function.
   *
   * @param token the JWT token
   * @param claimResolver a function that defines which claim to extract
   * @param <T> the type of the extracted claim
   * @return the resolved claim
   */
  private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
    Claims claims = extractAllClaims(token);
    return claimResolver.apply(claims);
  }

  /**
   * Parses the JWT token and retrieves all claims.
   *
   * @param token the JWT token
   * @return the {@link Claims} object containing all token data
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSingingKey()).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Generates a {@link SecretKey} from the configured secret key string for JWT signature verification.
   *
   * @return the generated {@link SecretKey}
   */
  private SecretKey getSingingKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}

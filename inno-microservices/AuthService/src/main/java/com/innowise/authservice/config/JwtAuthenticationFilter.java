package com.innowise.authservice.config;

import com.innowise.authservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Servlet filter that authenticates requests based on a JWT provided in the {@code Authorization}
 * header (expects the header to start with {@code "Bearer "}).
 *
 * <p>The filter:
 *
 * <ul>
 *   <li>Extracts the JWT token from the {@code Authorization} header.
 *   <li>Extracts the username from the token via {@link #jwtService}.
 *   <li>Loads {@link UserDetails} using {@link #userDetailsService} and, if the token is valid,
 *       creates a {@link UsernamePasswordAuthenticationToken} and sets it into the
 *       {@link SecurityContextHolder}.
 *   <li>If any exception occurs while processing the token, delegates exception handling to the
 *       provided {@link HandlerExceptionResolver} so Spring MVC can render an appropriate response.
 * </ul>
 *
 * <p>Dependencies are injected via constructor (Lombok's {@code @RequiredArgsConstructor}).
 *
 * @see OncePerRequestFilter
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final Integer BEARER_END = 7;
  private static final String HEADER = "Authorization";
  private static final String HEADER_START = "Bearer ";

  private final HandlerExceptionResolver handlerExceptionResolver;

  private final JwtService jwtService;

  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    final String authHeader = request.getHeader(HEADER);

    if (authHeader == null || !authHeader.startsWith(HEADER_START)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String jwt = authHeader.substring(BEARER_END);
      final String username = jwtService.extractUsername(jwt);

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (username != null && authentication == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }

      filterChain.doFilter(request, response);
    } catch (Exception ex) {
      handlerExceptionResolver.resolveException(request, response, null, ex);
    }
  }
}

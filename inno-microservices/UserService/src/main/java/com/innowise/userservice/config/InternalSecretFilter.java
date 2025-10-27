package com.innowise.userservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InternalSecretFilter extends OncePerRequestFilter {

  private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

  @Value("${security.jwt.secret-key}")
  private String expectedSecret;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      String secret = request.getHeader(INTERNAL_SECRET_HEADER);

      if (request.getRequestURI().startsWith("/api/v1/users")) {

        if (secret != null && secret.equals(expectedSecret)) {
          List<SimpleGrantedAuthority> authorities =
              List.of(new SimpleGrantedAuthority("ROLE_SERVICE"));

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken("internal-service", null, authorities);

          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}

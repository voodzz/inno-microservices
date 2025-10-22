package com.innowise.userservice.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTests {

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Sets authentication when valid JWT provided with roles")
  void setsAuthenticationOnValidToken() throws ServletException, IOException {
    HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(resolver);

    byte[] key = Jwts.SIG.HS256.key().build().getEncoded();
    String base64 = Encoders.BASE64.encode(key);
    ReflectionTestUtils.setField(filter, "secretKey", base64);

    String jwt =
        Jwts.builder()
            .subject("user1")
            .claim("roles", List.of("ROLE_USER"))
            .expiration(new Date(System.currentTimeMillis() + 60_000))
            .signWith(Keys.hmacShaKeyFor(key))
            .compact();

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + jwt);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals("user1", SecurityContextHolder.getContext().getAuthentication().getName());
    assertTrue(
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    verify(chain).doFilter(request, response);
  }

  @Test
  @DisplayName("Delegates to resolver on malformed token")
  void delegatesOnException() throws ServletException, IOException {
    HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(resolver);
    ReflectionTestUtils.setField(
        filter,
        "secretKey",
        Encoders.BASE64.encode("dummysecretkeydummysecretkeydummy".getBytes()));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer invalid.token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(resolver).resolveException(eq(request), eq(response), isNull(), any(Exception.class));
    verify(chain).doFilter(request, response);
  }
}

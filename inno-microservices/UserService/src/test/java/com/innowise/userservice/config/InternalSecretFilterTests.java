package com.innowise.userservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalSecretFilterTests {

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Sets ROLE_SERVICE when header matches secret and path is /api/v1/users")
  void setsAuthenticationOnMatch() throws ServletException, IOException {
    InternalSecretFilter filter = new InternalSecretFilter();
    ReflectionTestUtils.setField(filter, "expectedSecret", "secret");

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    when(request.getHeader("X-Internal-Secret")).thenReturn("secret");
    when(request.getRequestURI()).thenReturn("/api/v1/users/1");

    filter.doFilter(request, response, chain);

    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertTrue(
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE")));
    verify(chain).doFilter(request, response);
  }

  @Test
  @DisplayName("Does not set auth when header missing or path doesn't match")
  void noAuthOnNoMatch() throws ServletException, IOException {
    InternalSecretFilter filter = new InternalSecretFilter();
    ReflectionTestUtils.setField(filter, "expectedSecret", "secret");

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    when(request.getHeader("X-Internal-Secret")).thenReturn(null);
    when(request.getRequestURI()).thenReturn("/api/v1/cards");

    filter.doFilter(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(chain).doFilter(request, response);
  }
}

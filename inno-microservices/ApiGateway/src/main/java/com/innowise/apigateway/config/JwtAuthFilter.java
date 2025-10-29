package com.innowise.apigateway.config;

import com.innowise.apigateway.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {
  private static final String BEARER = "Bearer ";
  private static final Integer TOKEN_START_INDEX = 7;
  private final JwtService jwtService;

  private static final List<String> openEndpoints =
      List.of("/api/v1/auth/login", "/api/v1/auth/register");

  @Autowired
  public JwtAuthFilter(JwtService jwtService) {
    super(Config.class);
    this.jwtService = jwtService;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();
      String path = request.getURI().getPath();

      if (isOpenPath(path)) {
        return chain.filter(exchange);
      }

      String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

      if (header == null || !header.startsWith(BEARER)) {
        return handleUnauthorized(exchange, "Missing or invalid authorization header.");
      }

      String token = header.substring(TOKEN_START_INDEX);

      if (!jwtService.isTokenValid(token)) {
        return handleUnauthorized(exchange, "Invalid token.");
      }

      return chain.filter(exchange);
    };
  }

  private boolean isOpenPath(String path) {
    return openEndpoints.stream().anyMatch(path::startsWith);
  }

  private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

    String body = String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message);
    return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
  }

  public static class Config {}
}

package com.innowise.apigateway.config;

import com.innowise.apigateway.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {
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

      String header = request.getHeaders().getFirst("Authorization");

      if (header == null || !header.startsWith("Bearer ")) {
        return handleUnauthorized(exchange, "Missing or invalid authorization header.");
      }

      try {
        String token = header.substring(7);

        if (!jwtService.isTokenValid(token)) {
          return handleUnauthorized(exchange, "Invalid token.");
        }

        String username = jwtService.extractUsername(token);
        ServerHttpRequest modifiedRequest =
            request.mutate().header("X-User-Name", username).build();
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
      } catch (Exception e) {
        return handleUnauthorized(exchange, "Token validation failed: " + e.getMessage());
      }
    };
  }

  private boolean isOpenPath(String path) {
    return openEndpoints.stream().anyMatch(path::startsWith);
  }

  private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().add("Content-Type", "application/json");

    String body = String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message);
    return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
  }

  public static class Config {}
}

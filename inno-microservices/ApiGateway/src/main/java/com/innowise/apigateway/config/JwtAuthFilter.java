package com.innowise.apigateway.config;

import com.innowise.apigateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * A custom Spring Cloud Gateway filter factory for JWT authentication. It validates the JWT token
 * in the Authorization header for protected endpoints. Open endpoints (login, register) are
 * excluded from the filter.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {
  private static final String BEARER = "Bearer ";
  private static final Integer TOKEN_START_INDEX = 7;
  private final JwtService jwtService;

  @Value("${security.open-endpoints}")
  private static List<String> openEndpoints;

  /**
   * Applies the JWT authentication logic to the gateway exchange. Checks if the path is open, then
   * validates the 'Bearer' token in the header. Responds with 401 Unauthorized for missing or
   * invalid tokens.
   *
   * @param config The configuration object for this filter (currently unused).
   * @return The GatewayFilter logic to execute.
   */
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

  /**
   * Checks if the given request path is an open endpoint that does not require JWT authentication.
   * The list of open endpoints is predefined.
   *
   * @param path The URI path of the incoming request.
   * @return {@code true} if the path is an open endpoint, {@code false} otherwise.
   */
  private boolean isOpenPath(String path) {
    return openEndpoints.stream().anyMatch(path::startsWith);
  }

  /**
   * Creates a reactive response with HTTP 401 Unauthorized status and a JSON body containing an
   * error message. Used when authentication fails.
   *
   * @param exchange The current server web exchange.
   * @param message The specific error message to include in the response body.
   * @return A {@code Mono<Void>} that completes when the response is written.
   */
  private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

    String body = String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message);
    return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
  }

  /**
   * Inner class for filter configuration (required by AbstractGatewayFilterFactory). This class is
   * empty as the filter doesn't require specific runtime properties.
   */
  public static class Config {}
}

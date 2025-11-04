package com.innowise.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the Spring Cloud Gateway. It defines the routing rules for various
 * microservices and applies the JWT authentication filter to protected routes.
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {
  private final JwtAuthFilter filter;

  @Value("${user.service.url}")
  private String userServiceUrl;

  @Value("${auth.service.url}")
  private String authServiceUrl;

  @Value("${order.service.url}")
  private String orderServiceUrl;

  /**
   * Defines the gateway routes using a RouteLocatorBuilder.
   *
   * @param builder The RouteLocatorBuilder provided by Spring Cloud Gateway.
   * @return A RouteLocator containing all defined routes.
   */
  @Bean
  public RouteLocator routeLocator(RouteLocatorBuilder builder) {
    return builder
        .routes()
        .route(
            "user-service",
            r ->
                r.path("/api/v1/users/**")
                    .filters(f -> f.filter(filter.apply(new JwtAuthFilter.Config())))
                    .uri(userServiceUrl))
        .route(
            "user-card-service",
            r ->
                r.path("/api/v1/cards/**")
                    .filters(f -> f.filter(filter.apply(new JwtAuthFilter.Config())))
                    .uri(userServiceUrl))
        .route(
            "auth-service",
            r ->
                r.path("/api/v1/auth/**")
                    .filters(f -> f.filter(filter.apply(new JwtAuthFilter.Config())))
                    .uri(authServiceUrl))
        .route(
            "order-service",
            r ->
                r.path("/api/v1/orders/**")
                    .filters(f -> f.filter(filter.apply(new JwtAuthFilter.Config())))
                    .uri(orderServiceUrl))
        .build();
  }
}

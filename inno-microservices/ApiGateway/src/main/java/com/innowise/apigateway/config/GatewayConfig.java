package com.innowise.apigateway.config;

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
public class GatewayConfig {
  private final JwtAuthFilter filter;
  private final String userServiceUrl;
  private final String authServiceUrl;
  private final String orderServiceUrl;

  /**
   * Constructs the GatewayConfig with required dependencies and service URLs.
   *
   * @param filter The JWT authentication filter to apply to protected routes.
   * @param userServiceUrl The base URL of the User Service.
   * @param authServiceUrl The base URL of the Auth Service.
   * @param orderServiceUrl The base URL of the Order Service.
   */
  public GatewayConfig(
      JwtAuthFilter filter,
      @Value("${user.service.url}") String userServiceUrl,
      @Value("${auth.service.url}") String authServiceUrl,
      @Value("${order.service.url}") String orderServiceUrl) {
    this.filter = filter;
    this.userServiceUrl = userServiceUrl;
    this.authServiceUrl = authServiceUrl;
    this.orderServiceUrl = orderServiceUrl;
  }

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
        .route("auth-service-login", r -> r.path("/api/v1/auth/login/**").uri(authServiceUrl))
        .route(
            "auth-service",
            r ->
                r.path("/api/v1/auth/**")
                    .and()
                    .not(p -> p.path("/api/v1/auth/register/**"))
                    .and()
                    .not(p -> p.path("/api/v1/auth/login/**"))
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

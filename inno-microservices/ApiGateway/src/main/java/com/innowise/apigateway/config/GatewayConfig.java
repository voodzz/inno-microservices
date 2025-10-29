package com.innowise.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
  private final JwtAuthFilter filter;
  private final String userServiceUrl;
  private final String authServiceUrl;
  private final String orderServiceUrl;

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

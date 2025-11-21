package com.innowise.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.api")
public record ExternalApiProperties(String url, Long connectTimeout, Long readTimeout) {}

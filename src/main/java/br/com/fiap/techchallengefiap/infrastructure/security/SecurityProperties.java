package br.com.fiap.techchallengefiap.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(String jwtSecret, Duration tokenValidity) {
}

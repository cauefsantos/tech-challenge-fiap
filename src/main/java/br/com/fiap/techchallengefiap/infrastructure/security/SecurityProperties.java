package br.com.fiap.techchallengefiap.infrastructure.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(@NotBlank String jwtSecret, @NotNull Duration tokenValidity) {
}

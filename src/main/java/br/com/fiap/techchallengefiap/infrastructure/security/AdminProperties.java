package br.com.fiap.techchallengefiap.infrastructure.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(min = 12, max = 72) String password
) {
}

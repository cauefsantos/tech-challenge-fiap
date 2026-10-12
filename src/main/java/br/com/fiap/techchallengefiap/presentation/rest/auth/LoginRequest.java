package br.com.fiap.techchallengefiap.presentation.rest.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}

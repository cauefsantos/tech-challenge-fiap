package br.com.fiap.oficina.presentation.rest.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}

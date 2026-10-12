package br.com.fiap.techchallengefiap.presentation.rest.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}

package br.com.fiap.techchallengefiap.presentation.rest.shared;

import jakarta.validation.constraints.NotNull;

public record AtivoRequest(@NotNull Boolean ativo) {
}

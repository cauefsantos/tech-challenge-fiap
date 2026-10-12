package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import jakarta.validation.constraints.NotNull;

public record CriarOrdemServicoRequest(@NotNull Long clienteId, @NotNull Long veiculoId) {
}

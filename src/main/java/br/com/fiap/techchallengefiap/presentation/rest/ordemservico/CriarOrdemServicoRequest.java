package br.com.fiap.oficina.presentation.rest.ordemservico;

import jakarta.validation.constraints.NotNull;

public record CriarOrdemServicoRequest(@NotNull Long clienteId, @NotNull Long veiculoId) {
}

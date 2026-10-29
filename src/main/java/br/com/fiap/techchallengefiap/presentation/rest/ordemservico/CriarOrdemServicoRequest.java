package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import jakarta.validation.constraints.NotBlank;

public record CriarOrdemServicoRequest(@NotBlank String documentoCliente, @NotBlank String placa) {
}

package br.com.fiap.techchallengefiap.presentation.rest.veiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VeiculoRequest(
        @NotBlank String documentoCliente,
        @NotBlank String placa,
        @NotBlank String marca,
        @NotBlank String modelo,
        @NotNull Integer ano) {
}

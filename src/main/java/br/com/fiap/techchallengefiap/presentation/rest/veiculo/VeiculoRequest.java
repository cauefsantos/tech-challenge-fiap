package br.com.fiap.techchallengefiap.presentation.rest.veiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VeiculoRequest(
        @NotNull @Positive Long clienteId,
        @NotBlank String placa,
        @NotBlank String marca,
        @NotBlank String modelo,
        @NotNull Integer ano) {
}

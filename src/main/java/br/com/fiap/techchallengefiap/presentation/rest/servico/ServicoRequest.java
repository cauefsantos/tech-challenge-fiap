package br.com.fiap.techchallengefiap.presentation.rest.servico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ServicoRequest(
        @NotBlank String codigo,
        @NotBlank String nome,
        String descricao,
        @NotNull BigDecimal valorBase,
        @NotNull Long tempoEstimadoSegundos) {
}

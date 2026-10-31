package br.com.fiap.techchallengefiap.presentation.rest.material;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EntradaEstoqueRequest(@NotNull BigDecimal quantidade) {
}

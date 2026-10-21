package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemMaterialRequest(@NotNull Long materialId, @NotNull @Positive BigDecimal quantidade) {
}

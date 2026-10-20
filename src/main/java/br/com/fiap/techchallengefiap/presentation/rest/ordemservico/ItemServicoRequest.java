package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemServicoRequest(@NotNull Long servicoId, @NotNull @Positive BigDecimal quantidade) {
}

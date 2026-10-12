package br.com.fiap.techchallengefiap.presentation.rest.material;

import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MaterialRequest(
        @NotBlank String codigo,
        @NotNull TipoMaterial tipo,
        @NotBlank String nome,
        String descricao,
        @NotNull BigDecimal valorUnitario,
        @NotBlank String unidadeMedida,
        @NotNull BigDecimal quantidadeEstoque,
        @NotNull BigDecimal estoqueMinimo) {
}

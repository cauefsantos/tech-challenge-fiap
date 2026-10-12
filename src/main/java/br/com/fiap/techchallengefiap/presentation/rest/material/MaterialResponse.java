package br.com.fiap.techchallengefiap.presentation.rest.material;

import br.com.fiap.techchallengefiap.domain.material.model.Material;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MaterialResponse(Long id, String codigo, String tipo, String nome, String descricao,
                               BigDecimal valorUnitario, String unidadeMedida, BigDecimal quantidadeEstoque,
                               BigDecimal estoqueMinimo, OffsetDateTime dataCadastro, boolean ativo) {
    public static MaterialResponse from(Material material) {
        return new MaterialResponse(material.getId(), material.getCodigo(), material.getTipo().name(),
                material.getNome(), material.getDescricao(), material.getValorUnitario(),
                material.getUnidadeMedida(), material.getQuantidadeEstoque(), material.getEstoqueMinimo(),
                material.getDataCadastro(), material.isAtivo());
    }
}

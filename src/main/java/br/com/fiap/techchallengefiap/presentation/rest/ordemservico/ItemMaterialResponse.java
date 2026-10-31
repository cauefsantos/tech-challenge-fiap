package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemMaterial;

import java.math.BigDecimal;

public record ItemMaterialResponse(Long id, Long materialId, TipoMaterial tipo, String descricao, BigDecimal quantidade,
                                   BigDecimal valorUnitario, BigDecimal subtotal) {
    public static ItemMaterialResponse from(ItemMaterial item) {
        return new ItemMaterialResponse(item.getId(), item.getMaterialId(), item.getTipo(), item.getDescricao(),
                item.getQuantidade(), item.getValorUnitario(), item.getSubtotal());
    }
}

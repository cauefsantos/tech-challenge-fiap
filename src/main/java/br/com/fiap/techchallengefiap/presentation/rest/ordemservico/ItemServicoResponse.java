package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemServico;

import java.math.BigDecimal;

public record ItemServicoResponse(Long id, Long servicoId, String descricao, BigDecimal quantidade,
                                  BigDecimal valorUnitario, BigDecimal subtotal) {
    public static ItemServicoResponse from(ItemServico item) {
        return new ItemServicoResponse(item.getId(), item.getServicoId(), item.getDescricao(), item.getQuantidade(),
                item.getValorUnitario(), item.getSubtotal());
    }
}

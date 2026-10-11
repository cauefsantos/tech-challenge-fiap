package br.com.fiap.oficina.presentation.rest.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.model.OrdemServico;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrdemServicoResponse(Long id, Long clienteId, Long veiculoId, String status, boolean orcamentoAprovado,
                                   BigDecimal valorTotal, OffsetDateTime criadaEm) {
    public static OrdemServicoResponse from(OrdemServico os) {
        return new OrdemServicoResponse(os.getId(), os.getClienteId(), os.getVeiculoId(), os.getStatus().name(), os.isOrcamentoAprovado(), os.getValorTotal(), os.getCriadaEm());
    }
}

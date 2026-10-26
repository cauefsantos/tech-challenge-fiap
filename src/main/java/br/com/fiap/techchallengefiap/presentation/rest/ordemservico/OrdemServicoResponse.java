package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import br.com.fiap.techchallengefiap.application.ordemservico.IdentificarOrdemServicoUseCase.IdentificacaoOrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrdemServicoResponse(Long id, String numero, String documentoCliente, String placa, String status,
                                   boolean orcamentoAprovado, BigDecimal valorTotal, OffsetDateTime criadaEm) {
    public static OrdemServicoResponse from(OrdemServico os, IdentificacaoOrdemServico identificacao) {
        return new OrdemServicoResponse(os.getId(), os.getNumero(), identificacao.documentoCliente(),
                identificacao.placa(), os.getStatus().name(), os.isOrcamentoAprovado(), os.getValorTotal(),
                os.getCriadaEm());
    }
}

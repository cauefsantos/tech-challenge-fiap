package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import br.com.fiap.techchallengefiap.domain.orcamento.model.Orcamento;
import br.com.fiap.techchallengefiap.domain.orcamento.model.StatusOrcamento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrcamentoResponse(Long id, Long ordemServicoId, int versao, BigDecimal valorTotal,
                               StatusOrcamento status, OffsetDateTime dataGeracao,
                               OffsetDateTime dataResposta, String observacaoResposta) {
    public static OrcamentoResponse from(Orcamento orcamento) {
        return new OrcamentoResponse(
                orcamento.getId(),
                orcamento.getOrdemServicoId(),
                orcamento.getVersao(),
                orcamento.getValorTotal(),
                orcamento.getStatus(),
                orcamento.getDataGeracao(),
                orcamento.getDataResposta(),
                orcamento.getObservacaoResposta());
    }
}

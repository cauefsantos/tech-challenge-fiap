package br.com.fiap.techchallengefiap.presentation.rest.servico;

import br.com.fiap.techchallengefiap.domain.servico.model.Servico;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ServicoResponse(Long id, String codigo, String nome, String descricao, BigDecimal valorBase,
                              long tempoEstimadoSegundos, OffsetDateTime dataCadastro, boolean ativo) {
    public static ServicoResponse from(Servico servico) {
        return new ServicoResponse(servico.getId(), servico.getCodigo(), servico.getNome(), servico.getDescricao(),
                servico.getValorBase(), servico.getTempoEstimado().toSeconds(), servico.getDataCadastro(), servico.isAtivo());
    }
}

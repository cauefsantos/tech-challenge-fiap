package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PublicOrdemServicoResponse(String numero, String status, BigDecimal valorTotal,
                                        OffsetDateTime dataInicioExecucao, OffsetDateTime dataFinalizacao,
                                        OffsetDateTime dataEntrega) {
    public static PublicOrdemServicoResponse from(OrdemServico os) {
        return new PublicOrdemServicoResponse(os.getNumero(), os.getStatus().name(), os.getValorTotal(),
                os.getDataInicioExecucao(), os.getDataFinalizacao(), os.getDataEntrega());
    }
}

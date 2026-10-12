package br.com.fiap.techchallengefiap.presentation.rest.veiculo;

import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;

import java.time.OffsetDateTime;

public record VeiculoResponse(Long id, Long clienteId, String placa, String marca, String modelo,
                              Integer ano, OffsetDateTime dataCadastro, boolean ativo) {
    public static VeiculoResponse from(Veiculo veiculo) {
        return new VeiculoResponse(veiculo.getId(), veiculo.getClienteId(), veiculo.getPlaca(), veiculo.getMarca(),
                veiculo.getModelo(), veiculo.getAno(), veiculo.getDataCadastro(), veiculo.isAtivo());
    }
}

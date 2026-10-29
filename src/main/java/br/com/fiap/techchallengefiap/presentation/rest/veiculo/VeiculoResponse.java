package br.com.fiap.techchallengefiap.presentation.rest.veiculo;

import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;

import java.time.OffsetDateTime;

public record VeiculoResponse(String placa, String documentoCliente, String marca, String modelo,
                              Integer ano, OffsetDateTime dataCadastro, boolean ativo) {
    public static VeiculoResponse from(Veiculo veiculo, String documentoCliente) {
        return new VeiculoResponse(veiculo.getPlaca(), documentoCliente, veiculo.getMarca(),
                veiculo.getModelo(), veiculo.getAno(), veiculo.getDataCadastro(), veiculo.isAtivo());
    }
}

package br.com.fiap.techchallengefiap.presentation.rest.cliente;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;

import java.time.OffsetDateTime;

public record ClienteResponse(String documento, String tipoPessoa, String nome, String email,
                              String telefone, OffsetDateTime dataCadastro, boolean ativo) {
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(cliente.getDocumento(), cliente.getTipoPessoa().name(),
                cliente.getNome(), cliente.getEmail(), cliente.getTelefone(), cliente.getDataCadastro(), cliente.isAtivo());
    }
}

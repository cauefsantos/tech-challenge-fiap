package br.com.fiap.techchallengefiap.domain.cliente.repository;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorId(Long id);
    /** Retorna o cliente ativo com o documento; se nao houver, o cadastro inativo mais recente. */
    Optional<Cliente> buscarPorDocumento(String documento);
    List<Cliente> listar();
    boolean existeDocumentoAtivo(String documento);
    boolean existeDocumentoAtivoParaOutroCliente(String documento, Long id);
}

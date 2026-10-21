package br.com.fiap.techchallengefiap.application.cliente;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GerenciarClienteUseCase {
    private final ClienteRepository repository;

    public GerenciarClienteUseCase(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Cliente criar(TipoPessoa tipoPessoa, String documento, String nome, String email, String telefone) {
        Cliente cliente = new Cliente(tipoPessoa, documento, nome, email, telefone);
        if (repository.existeDocumentoAtivo(cliente.getDocumento())) {
            throw new ConflictException("Ja existe um cliente ativo com este documento.");
        }
        return repository.salvar(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return repository.buscarPorId(id).orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return repository.listar();
    }

    @Transactional
    public Cliente atualizar(Long id, TipoPessoa tipoPessoa, String documento, String nome, String email, String telefone) {
        Cliente existente = buscar(id);
        Cliente atualizado = new Cliente(id, tipoPessoa, documento, nome, email, telefone,
                existente.getDataCadastro(), existente.isAtivo());
        if (atualizado.isAtivo() && repository.existeDocumentoAtivoParaOutroCliente(atualizado.getDocumento(), id)) {
            throw new ConflictException("Ja existe um cliente ativo com este documento.");
        }
        return repository.salvar(atualizado);
    }

    @Transactional
    public Cliente alterarAtivo(Long id, boolean ativo) {
        Cliente cliente = buscar(id);
        if (ativo && repository.existeDocumentoAtivoParaOutroCliente(cliente.getDocumento(), id)) {
            throw new ConflictException("Ja existe um cliente ativo com este documento.");
        }
        if (ativo) cliente.ativar(); else cliente.desativar();
        return repository.salvar(cliente);
    }
}

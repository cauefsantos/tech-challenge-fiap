package br.com.fiap.techchallengefiap.infrastructure.cliente;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClienteRepositoryAdapter implements ClienteRepository {
    private final ClienteJpaRepository jpa;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return toDomain(jpa.save(toEntity(cliente)));
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        return jpa.findFirstByDocumentoOrderByAtivoDescIdDesc(documento).map(this::toDomain);
    }

    @Override
    public List<Cliente> listar() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existeDocumentoAtivo(String documento) {
        return jpa.existsByDocumentoAndAtivoTrue(documento);
    }

    @Override
    public boolean existeDocumentoAtivoParaOutroCliente(String documento, Long id) {
        return jpa.existsByDocumentoAndAtivoTrueAndIdNot(documento, id);
    }

    private ClienteJpaEntity toEntity(Cliente cliente) {
        var entity = new ClienteJpaEntity();
        entity.id = cliente.getId();
        entity.tipoPessoa = cliente.getTipoPessoa();
        entity.documento = cliente.getDocumento();
        entity.nome = cliente.getNome();
        entity.email = cliente.getEmail();
        entity.telefone = cliente.getTelefone();
        entity.dataCadastro = cliente.getDataCadastro();
        entity.ativo = cliente.isAtivo();
        return entity;
    }

    private Cliente toDomain(ClienteJpaEntity entity) {
        return new Cliente(entity.id, entity.tipoPessoa, entity.documento, entity.nome, entity.email,
                entity.telefone, entity.dataCadastro, entity.ativo);
    }
}

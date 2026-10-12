package br.com.fiap.techchallengefiap.infrastructure.servico;

import br.com.fiap.techchallengefiap.domain.servico.model.Servico;
import br.com.fiap.techchallengefiap.domain.servico.repository.ServicoRepository;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Repository
public class ServicoRepositoryAdapter implements ServicoRepository {
    private final ServicoJpaRepository jpa;

    public ServicoRepositoryAdapter(ServicoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Servico salvar(Servico servico) {
        return toDomain(jpa.save(toEntity(servico)));
    }

    @Override
    public Optional<Servico> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Servico> listar() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return jpa.existsByCodigo(codigo);
    }

    @Override
    public boolean existeCodigoParaOutroServico(String codigo, Long id) {
        return jpa.existsByCodigoAndIdNot(codigo, id);
    }

    private ServicoJpaEntity toEntity(Servico servico) {
        var entity = new ServicoJpaEntity();
        entity.id = servico.getId();
        entity.codigo = servico.getCodigo();
        entity.nome = servico.getNome();
        entity.descricao = servico.getDescricao();
        entity.valorBase = servico.getValorBase();
        entity.tempoEstimadoSegundos = servico.getTempoEstimado().toSeconds();
        entity.dataCadastro = servico.getDataCadastro();
        entity.ativo = servico.isAtivo();
        return entity;
    }

    private Servico toDomain(ServicoJpaEntity entity) {
        return new Servico(entity.id, entity.codigo, entity.nome, entity.descricao, entity.valorBase,
                Duration.ofSeconds(entity.tempoEstimadoSegundos), entity.dataCadastro, entity.ativo);
    }
}

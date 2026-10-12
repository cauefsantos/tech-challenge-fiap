package br.com.fiap.techchallengefiap.infrastructure.ordemservico;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrdemServicoRepositoryAdapter implements OrdemServicoRepository {
    private final OrdemServicoJpaRepository jpa;

    public OrdemServicoRepositoryAdapter(OrdemServicoJpaRepository jpa) {
        this.jpa = jpa;
    }

    public OrdemServico salvar(OrdemServico os) {
        return toDomain(jpa.save(toEntity(os)));
    }

    public Optional<OrdemServico> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    private OrdemServicoJpaEntity toEntity(OrdemServico os) {
        var e = new OrdemServicoJpaEntity();
        e.id = os.getId();
        e.clienteId = os.getClienteId();
        e.veiculoId = os.getVeiculoId();
        e.numero = os.getNumero();
        e.status = os.getStatus();
        e.orcamentoAprovado = os.isOrcamentoAprovado();
        e.valorTotal = os.getValorTotal();
        e.criadaEm = os.getCriadaEm();
        e.descricaoProblema = os.getDescricaoProblema();
        e.diagnostico = os.getDiagnostico();
        e.dataInicioExecucao = os.getDataInicioExecucao();
        e.dataFinalizacao = os.getDataFinalizacao();
        e.dataEntrega = os.getDataEntrega();
        e.observacoes = os.getObservacoes();
        return e;
    }

    private OrdemServico toDomain(OrdemServicoJpaEntity e) {
        return new OrdemServico(e.id, e.numero, e.clienteId, e.veiculoId, e.status, e.orcamentoAprovado,
                e.valorTotal, e.criadaEm, e.descricaoProblema, e.diagnostico, e.dataInicioExecucao,
                e.dataFinalizacao, e.dataEntrega, e.observacoes);
    }
}

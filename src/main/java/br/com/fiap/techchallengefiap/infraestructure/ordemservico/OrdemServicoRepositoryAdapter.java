package br.com.fiap.oficina.infrastructure.persistence.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.model.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.repository.OrdemServicoRepository;
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
        e.status = os.getStatus();
        e.orcamentoAprovado = os.isOrcamentoAprovado();
        e.valorTotal = os.getValorTotal();
        e.criadaEm = os.getCriadaEm();
        return e;
    }

    private OrdemServico toDomain(OrdemServicoJpaEntity e) {
        return new OrdemServico(e.id, e.clienteId, e.veiculoId, e.status, e.orcamentoAprovado, e.valorTotal, e.criadaEm);
    }
}

package br.com.fiap.techchallengefiap.application.servico;

import br.com.fiap.techchallengefiap.domain.servico.model.Servico;
import br.com.fiap.techchallengefiap.domain.servico.repository.ServicoRepository;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class GerenciarServicoUseCase {
    private final ServicoRepository repository;

    public GerenciarServicoUseCase(ServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Servico criar(String codigo, String nome, String descricao, BigDecimal valorBase, Long tempoEstimadoSegundos) {
        Servico servico = novoServico(null, codigo, nome, descricao, valorBase, tempoEstimadoSegundos,
                OffsetDateTime.now(), true);
        if (repository.existeCodigo(servico.getCodigo())) throw new ConflictException("Ja existe um servico com este codigo.");
        return repository.salvar(servico);
    }

    @Transactional(readOnly = true)
    public Servico buscar(Long id) {
        return repository.buscarPorId(id).orElseThrow(() -> new ResourceNotFoundException("Servico nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Servico> listar() {
        return repository.listar();
    }

    @Transactional
    public Servico atualizar(Long id, String codigo, String nome, String descricao, BigDecimal valorBase, Long tempoEstimadoSegundos) {
        Servico existente = buscar(id);
        Servico atualizado = novoServico(id, codigo, nome, descricao, valorBase, tempoEstimadoSegundos,
                existente.getDataCadastro(), existente.isAtivo());
        if (repository.existeCodigoParaOutroServico(atualizado.getCodigo(), id)) {
            throw new ConflictException("Ja existe um servico com este codigo.");
        }
        return repository.salvar(atualizado);
    }

    @Transactional
    public Servico alterarAtivo(Long id, boolean ativo) {
        Servico servico = buscar(id);
        if (ativo) servico.ativar(); else servico.desativar();
        return repository.salvar(servico);
    }

    private Servico novoServico(Long id, String codigo, String nome, String descricao, BigDecimal valorBase,
                                Long tempoEstimadoSegundos, OffsetDateTime dataCadastro, boolean ativo) {
        if (tempoEstimadoSegundos == null) {
            throw new DomainException("O tempo estimado e obrigatorio.");
        }
        return new Servico(id, codigo, nome, descricao, valorBase, Duration.ofSeconds(tempoEstimadoSegundos),
                dataCadastro, ativo);
    }
}

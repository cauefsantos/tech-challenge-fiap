package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import org.springframework.stereotype.Service;

@Service
public class BuscarOrdemServicoUseCase {
    private final OrdemServicoRepository repository;

    public BuscarOrdemServicoUseCase(OrdemServicoRepository repository) {
        this.repository = repository;
    }

    public OrdemServico executar(Long id) {
        return repository.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Ordem de servico nao encontrada."));
    }
}

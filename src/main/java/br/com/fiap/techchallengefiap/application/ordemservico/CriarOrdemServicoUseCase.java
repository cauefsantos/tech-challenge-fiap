package br.com.fiap.oficina.application.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.model.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.repository.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarOrdemServicoUseCase {
    private final OrdemServicoRepository repository;

    public CriarOrdemServicoUseCase(OrdemServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrdemServico executar(CriarOrdemServicoCommand command) {
        return repository.salvar(new OrdemServico(command.clienteId(), command.veiculoId()));
    }
}

package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import br.com.fiap.techchallengefiap.domain.veiculo.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarOrdemServicoUseCase {
    private final OrdemServicoRepository repository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;

    public CriarOrdemServicoUseCase(OrdemServicoRepository repository, ClienteRepository clienteRepository,
                                    VeiculoRepository veiculoRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional
    public OrdemServico executar(CriarOrdemServicoCommand command) {
        var cliente = clienteRepository.buscarPorId(command.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente da ordem de servico nao encontrado."));
        if (!cliente.isAtivo()) throw new DomainException("O cliente da ordem de servico deve estar ativo.");
        var veiculo = veiculoRepository.buscarPorId(command.veiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo da ordem de servico nao encontrado."));
        if (!veiculo.isAtivo()) throw new DomainException("O veiculo da ordem de servico deve estar ativo.");
        if (!veiculo.getClienteId().equals(cliente.getId())) {
            throw new DomainException("O veiculo informado nao pertence ao cliente da ordem de servico.");
        }
        return repository.salvar(new OrdemServico(command.clienteId(), command.veiculoId()));
    }
}

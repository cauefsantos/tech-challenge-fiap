package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;
import br.com.fiap.techchallengefiap.domain.veiculo.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Traduz as referencias internas da OS (ids de cliente e veiculo) para as identificacoes de negocio
 * expostas pela API: CPF/CNPJ do cliente e placa do veiculo.
 */
@Service
public class IdentificarOrdemServicoUseCase {
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;

    public IdentificarOrdemServicoUseCase(ClienteRepository clienteRepository, VeiculoRepository veiculoRepository) {
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional(readOnly = true)
    public IdentificacaoOrdemServico executar(OrdemServico os) {
        String documento = clienteRepository.buscarPorId(os.getClienteId())
                .map(Cliente::getDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente da ordem de servico nao encontrado."));
        String placa = veiculoRepository.buscarPorId(os.getVeiculoId())
                .map(Veiculo::getPlaca)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo da ordem de servico nao encontrado."));
        return new IdentificacaoOrdemServico(documento, placa);
    }

    public record IdentificacaoOrdemServico(String documentoCliente, String placa) {
    }
}

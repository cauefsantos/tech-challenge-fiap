package br.com.fiap.techchallengefiap.application.veiculo;

import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;
import br.com.fiap.techchallengefiap.domain.veiculo.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GerenciarVeiculoUseCase {
    private final VeiculoRepository repository;
    private final ClienteRepository clienteRepository;

    public GerenciarVeiculoUseCase(VeiculoRepository repository, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Veiculo criar(Long clienteId, String placa, String marca, String modelo, Integer ano) {
        validarClienteAtivo(clienteId);
        Veiculo veiculo = new Veiculo(clienteId, placa, marca, modelo, ano);
        if (repository.existePlacaAtiva(veiculo.getPlaca())) {
            throw new ConflictException("Ja existe um veiculo ativo com esta placa.");
        }
        return repository.salvar(veiculo);
    }

    @Transactional(readOnly = true)
    public Veiculo buscar(Long id) {
        return repository.buscarPorId(id).orElseThrow(() -> new ResourceNotFoundException("Veiculo nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listar() {
        return repository.listar();
    }

    @Transactional
    public Veiculo atualizar(Long id, Long clienteId, String placa, String marca, String modelo, Integer ano) {
        Veiculo existente = buscar(id);
        validarClienteAtivo(clienteId);
        Veiculo atualizado = new Veiculo(id, clienteId, placa, marca, modelo, ano,
                existente.getDataCadastro(), existente.isAtivo());
        if (atualizado.isAtivo() && repository.existePlacaAtivaParaOutroVeiculo(atualizado.getPlaca(), id)) {
            throw new ConflictException("Ja existe um veiculo ativo com esta placa.");
        }
        return repository.salvar(atualizado);
    }

    @Transactional
    public Veiculo alterarAtivo(Long id, boolean ativo) {
        Veiculo veiculo = buscar(id);
        if (ativo) {
            validarClienteAtivo(veiculo.getClienteId());
            if (repository.existePlacaAtivaParaOutroVeiculo(veiculo.getPlaca(), id)) {
                throw new ConflictException("Ja existe um veiculo ativo com esta placa.");
            }
            veiculo.ativar();
        } else {
            veiculo.desativar();
        }
        return repository.salvar(veiculo);
    }

    private void validarClienteAtivo(Long clienteId) {
        var cliente = clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente do veiculo nao encontrado."));
        if (!cliente.isAtivo()) throw new DomainException("O veiculo deve estar vinculado a um cliente ativo.");
    }
}

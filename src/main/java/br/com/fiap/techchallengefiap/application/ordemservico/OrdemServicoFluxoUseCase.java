package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.repository.MaterialRepository;
import br.com.fiap.techchallengefiap.domain.orcamento.model.Orcamento;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import br.com.fiap.techchallengefiap.domain.servico.repository.ServicoRepository;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.TreeMap;

@Service
public class OrdemServicoFluxoUseCase {
    private final OrdemServicoRepository repository;
    private final ServicoRepository servicoRepository;
    private final MaterialRepository materialRepository;

    public OrdemServicoFluxoUseCase(OrdemServicoRepository repository,
                                   ServicoRepository servicoRepository,
                                   MaterialRepository materialRepository) {
        this.repository = repository;
        this.servicoRepository = servicoRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional
    public OrdemServico enviarParaDiagnostico(Long id) {
        var os = buscar(id);
        os.enviarParaDiagnostico();
        return repository.salvar(os);
    }

    @Transactional
    public OrdemServico registrarDiagnostico(Long id, String diagnostico) {
        var os = buscar(id);
        os.registrarDiagnostico(diagnostico);
        return repository.salvar(os);
    }

    @Transactional
    public OrdemServico concluirDiagnostico(Long id) {
        var os = buscar(id);
        os.concluirDiagnostico();
        return repository.salvar(os);
    }

    @Transactional
    public OrdemServico adicionarItemServico(Long ordemServicoId, Long servicoId, BigDecimal quantidade) {
        var os = buscar(ordemServicoId);
        var servico = servicoRepository.buscarPorId(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Servico nao encontrado."));
        os.adicionarItemServico(new ItemServico(os.getId(), servico, quantidade));
        return repository.salvar(os);
    }

    @Transactional
    public OrdemServico removerItemServico(Long ordemServicoId, Long itemId) {
        var os = buscar(ordemServicoId);
        os.removerItemServico(itemId);
        return repository.salvar(os);
    }

    @Transactional
    public OrdemServico adicionarItemMaterial(Long ordemServicoId, Long materialId, BigDecimal quantidade) {
        var os = buscar(ordemServicoId);
        var material = materialRepository.buscarPorId(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material nao encontrado."));
        os.adicionarItemMaterial(new ItemMaterial(os.getId(), material, quantidade));
        return repository.salvar(os);
    }

    @Transactional
    public OrdemServico removerItemMaterial(Long ordemServicoId, Long itemId) {
        var os = buscar(ordemServicoId);
        os.removerItemMaterial(itemId);
        return repository.salvar(os);
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listar() {
        return repository.listar();
    }

    @Transactional
    public Orcamento gerarOrcamento(Long id) {
        var os = buscar(id);
        var orcamento = os.gerarOrcamento();
        var salva = repository.salvar(os);
        return salva.getOrcamentos().stream()
                .filter(item -> item.getVersao() == orcamento.getVersao())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Orcamento gerado nao foi persistido."));
    }

    @Transactional
    public OrdemServico disponibilizarOrcamento(Long id, Long orcamentoId) {
        var os = buscar(id);
        var orcamento = os.getOrcamentos().stream()
                .filter(item -> item.getId() != null && item.getId().equals(orcamentoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Orcamento nao encontrado nesta OS."));
        os.disponibilizarOrcamento(orcamento);
        return repository.salvar(os);
    }

    @Transactional
    public OrdemServico aprovarOrcamento(Long id, Long orcamentoId, String observacao) {
        var os = buscar(id);
        var quantidades = new TreeMap<Long, BigDecimal>();
        for (var item : os.getItensMaterial()) {
            quantidades.merge(item.getMaterialId(), item.getQuantidade(), BigDecimal::add);
        }
        var materiais = new LinkedHashMap<Long, Material>();
        var itensInsuficientes = new ArrayList<String>();

        for (var entrada : quantidades.entrySet()) {
            var material = materialRepository.buscarPorIdParaAtualizacao(entrada.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Material nao encontrado."));
            materiais.put(entrada.getKey(), material);
            try {
                material.validarDisponibilidade(entrada.getValue());
            } catch (DomainException ex) {
                itensInsuficientes.add(material.getNome());
            }
        }

        if (!itensInsuficientes.isEmpty()) {
            throw new DomainException("Estoque insuficiente para os materiais: " + String.join(", ", itensInsuficientes) + ".");
        }

        os.aprovarOrcamento(orcamentoId, observacao);

        for (var entrada : materiais.entrySet()) {
            var material = entrada.getValue();
            material.consumir(quantidades.get(entrada.getKey()));
            materialRepository.salvar(material);
        }

        return repository.salvar(os);
    }

    @Transactional
    public OrdemServico rejeitarOrcamento(Long id, Long orcamentoId, String observacao) {
        var os = buscar(id);
        os.rejeitarOrcamento(orcamentoId, observacao);
        return repository.salvar(os);
    }

    @Transactional
    public OrdemServico finalizar(Long id) {
        var os = buscar(id);
        os.finalizar();
        return repository.salvar(os);
    }

    @Transactional
    public OrdemServico entregar(Long id) {
        var os = buscar(id);
        os.entregar();
        return repository.salvar(os);
    }

    private OrdemServico buscar(Long id) {
        return repository.buscarPorIdParaAtualizacao(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de servico nao encontrada."));
    }
}

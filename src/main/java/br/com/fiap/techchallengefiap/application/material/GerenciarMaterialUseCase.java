package br.com.fiap.techchallengefiap.application.material;

import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.material.repository.MaterialRepository;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GerenciarMaterialUseCase {
    private final MaterialRepository repository;

    public GerenciarMaterialUseCase(MaterialRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Material criar(String codigo, TipoMaterial tipo, String nome, String descricao, BigDecimal valorUnitario,
                         String unidadeMedida, BigDecimal quantidadeEstoque, BigDecimal estoqueMinimo) {
        Material material = new Material(codigo, tipo, nome, descricao, valorUnitario, unidadeMedida,
                quantidadeEstoque, estoqueMinimo);
        if (repository.existeCodigo(material.getCodigo())) throw new ConflictException("Ja existe um material com este codigo.");
        return repository.salvar(material);
    }

    @Transactional(readOnly = true)
    public Material buscar(Long id) {
        return repository.buscarPorId(id).orElseThrow(() -> new ResourceNotFoundException("Material nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Material> listar() {
        return repository.listar();
    }

    @Transactional
    public Material atualizar(Long id, String codigo, TipoMaterial tipo, String nome, String descricao,
                              BigDecimal valorUnitario, String unidadeMedida, BigDecimal estoqueMinimo) {
        Material existente = buscarParaAtualizacao(id);
        // O saldo nao vem do cliente: uma edicao feita sobre uma leitura antiga desfaria baixas ja aprovadas.
        Material atualizado = new Material(id, codigo, tipo, nome, descricao, valorUnitario, unidadeMedida,
                existente.getQuantidadeEstoque(), estoqueMinimo, existente.getDataCadastro(), existente.isAtivo());
        if (repository.existeCodigoParaOutroMaterial(atualizado.getCodigo(), id)) {
            throw new ConflictException("Ja existe um material com este codigo.");
        }
        return repository.salvar(atualizado);
    }

    @Transactional
    public Material alterarAtivo(Long id, boolean ativo) {
        Material material = buscarParaAtualizacao(id);
        if (ativo) material.ativar(); else material.desativar();
        return repository.salvar(material);
    }

    @Transactional
    public Material registrarEntrada(Long id, BigDecimal quantidade) {
        Material material = buscarParaAtualizacao(id);
        material.registrarEntrada(quantidade);
        return repository.salvar(material);
    }

    private Material buscarParaAtualizacao(Long id) {
        return repository.buscarPorIdParaAtualizacao(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material nao encontrado."));
    }
}

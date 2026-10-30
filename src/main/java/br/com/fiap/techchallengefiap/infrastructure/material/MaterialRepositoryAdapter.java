package br.com.fiap.techchallengefiap.infrastructure.material;

import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.repository.MaterialRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MaterialRepositoryAdapter implements MaterialRepository {
    private final MaterialJpaRepository jpa;

    public MaterialRepositoryAdapter(MaterialJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Material salvar(Material material) {
        return toDomain(jpa.save(toEntity(material)));
    }

    @Override
    public Optional<Material> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Material> buscarPorIdParaAtualizacao(Long id) {
        return jpa.buscarPorIdParaAtualizacao(id).map(this::toDomain);
    }

    @Override
    public List<Material> listar() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return jpa.existsByCodigo(codigo);
    }

    @Override
    public boolean existeCodigoParaOutroMaterial(String codigo, Long id) {
        return jpa.existsByCodigoAndIdNot(codigo, id);
    }

    private MaterialJpaEntity toEntity(Material material) {
        var entity = new MaterialJpaEntity();
        entity.id = material.getId();
        entity.codigo = material.getCodigo();
        entity.tipo = material.getTipo();
        entity.nome = material.getNome();
        entity.descricao = material.getDescricao();
        entity.valorUnitario = material.getValorUnitario();
        entity.unidadeMedida = material.getUnidadeMedida();
        entity.quantidadeEstoque = material.getQuantidadeEstoque();
        entity.estoqueMinimo = material.getEstoqueMinimo();
        entity.dataCadastro = material.getDataCadastro();
        entity.ativo = material.isAtivo();
        return entity;
    }

    private Material toDomain(MaterialJpaEntity entity) {
        return new Material(entity.id, entity.codigo, entity.tipo, entity.nome, entity.descricao,
                entity.valorUnitario, entity.unidadeMedida, entity.quantidadeEstoque, entity.estoqueMinimo,
                entity.dataCadastro, entity.ativo);
    }
}

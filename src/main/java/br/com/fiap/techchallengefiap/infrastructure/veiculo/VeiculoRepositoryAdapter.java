package br.com.fiap.techchallengefiap.infrastructure.veiculo;

import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;
import br.com.fiap.techchallengefiap.domain.veiculo.repository.VeiculoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VeiculoRepositoryAdapter implements VeiculoRepository {
    private final VeiculoJpaRepository jpa;

    public VeiculoRepositoryAdapter(VeiculoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        return toDomain(jpa.save(toEntity(veiculo)));
    }

    @Override
    public Optional<Veiculo> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Veiculo> listar() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existePlacaAtiva(String placa) {
        return jpa.existsByPlacaAndAtivoTrue(placa);
    }

    @Override
    public boolean existePlacaAtivaParaOutroVeiculo(String placa, Long id) {
        return jpa.existsByPlacaAndAtivoTrueAndIdNot(placa, id);
    }

    private VeiculoJpaEntity toEntity(Veiculo veiculo) {
        var entity = new VeiculoJpaEntity();
        entity.id = veiculo.getId();
        entity.clienteId = veiculo.getClienteId();
        entity.placa = veiculo.getPlaca();
        entity.marca = veiculo.getMarca();
        entity.modelo = veiculo.getModelo();
        entity.ano = veiculo.getAno();
        entity.dataCadastro = veiculo.getDataCadastro();
        entity.ativo = veiculo.isAtivo();
        return entity;
    }

    private Veiculo toDomain(VeiculoJpaEntity entity) {
        return new Veiculo(entity.id, entity.clienteId, entity.placa, entity.marca, entity.modelo,
                entity.ano, entity.dataCadastro, entity.ativo);
    }
}

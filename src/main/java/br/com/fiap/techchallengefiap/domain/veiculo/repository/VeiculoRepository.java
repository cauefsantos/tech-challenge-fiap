package br.com.fiap.techchallengefiap.domain.veiculo.repository;

import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository {
    Veiculo salvar(Veiculo veiculo);
    Optional<Veiculo> buscarPorId(Long id);
    List<Veiculo> listar();
    boolean existePlacaAtiva(String placa);
    boolean existePlacaAtivaParaOutroVeiculo(String placa, Long id);
}

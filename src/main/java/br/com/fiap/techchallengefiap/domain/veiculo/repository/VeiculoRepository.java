package br.com.fiap.techchallengefiap.domain.veiculo.repository;

import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository {
    Veiculo salvar(Veiculo veiculo);
    Optional<Veiculo> buscarPorId(Long id);
    /** Retorna o veiculo ativo com a placa; se nao houver, o cadastro inativo mais recente. */
    Optional<Veiculo> buscarPorPlaca(String placa);
    List<Veiculo> listar();
    boolean existePlacaAtiva(String placa);
    boolean existePlacaAtivaParaOutroVeiculo(String placa, Long id);
}

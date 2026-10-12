package br.com.fiap.techchallengefiap.domain.servico.repository;

import br.com.fiap.techchallengefiap.domain.servico.model.Servico;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository {
    Servico salvar(Servico servico);
    Optional<Servico> buscarPorId(Long id);
    List<Servico> listar();
    boolean existeCodigo(String codigo);
    boolean existeCodigoParaOutroServico(String codigo, Long id);
}

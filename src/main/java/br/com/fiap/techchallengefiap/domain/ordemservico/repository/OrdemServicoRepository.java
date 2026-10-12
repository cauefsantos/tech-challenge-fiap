package br.com.fiap.techchallengefiap.domain.ordemservico.repository;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;

import java.util.Optional;

public interface OrdemServicoRepository {
    OrdemServico salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(Long id);
}

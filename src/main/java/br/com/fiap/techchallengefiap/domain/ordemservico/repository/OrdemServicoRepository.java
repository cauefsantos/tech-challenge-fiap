package br.com.fiap.oficina.domain.ordemservico.repository;

import br.com.fiap.oficina.domain.ordemservico.model.OrdemServico;

import java.util.Optional;

public interface OrdemServicoRepository {
    OrdemServico salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(Long id);
}

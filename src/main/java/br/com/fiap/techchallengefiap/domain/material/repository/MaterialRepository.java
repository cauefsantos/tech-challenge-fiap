package br.com.fiap.techchallengefiap.domain.material.repository;

import br.com.fiap.techchallengefiap.domain.material.model.Material;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository {
    Material salvar(Material material);
    Optional<Material> buscarPorId(Long id);
    List<Material> listar();
    boolean existeCodigo(String codigo);
    boolean existeCodigoParaOutroMaterial(String codigo, Long id);
}

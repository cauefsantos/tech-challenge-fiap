package br.com.fiap.techchallengefiap.infrastructure.servico;

import org.springframework.data.jpa.repository.JpaRepository;

interface ServicoJpaRepository extends JpaRepository<ServicoJpaEntity, Long> {
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
}

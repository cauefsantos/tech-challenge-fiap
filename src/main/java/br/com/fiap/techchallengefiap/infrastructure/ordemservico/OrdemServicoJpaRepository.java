package br.com.fiap.techchallengefiap.infrastructure.ordemservico;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoJpaEntity, Long> {
}

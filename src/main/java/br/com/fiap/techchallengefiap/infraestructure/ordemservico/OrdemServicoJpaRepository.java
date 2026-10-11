package br.com.fiap.oficina.infrastructure.persistence.ordemservico;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoJpaEntity, Long> {
}

package br.com.fiap.techchallengefiap.infrastructure.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, Long> {
    Optional<ClienteJpaEntity> findFirstByDocumentoOrderByAtivoDescIdDesc(String documento);
    boolean existsByDocumentoAndAtivoTrue(String documento);
    boolean existsByDocumentoAndAtivoTrueAndIdNot(String documento, Long id);
}

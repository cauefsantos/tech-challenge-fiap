package br.com.fiap.techchallengefiap.infrastructure.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, Long> {
    boolean existsByDocumentoAndAtivoTrue(String documento);
    boolean existsByDocumentoAndAtivoTrueAndIdNot(String documento, Long id);
}

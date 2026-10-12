package br.com.fiap.techchallengefiap.infrastructure.material;

import org.springframework.data.jpa.repository.JpaRepository;

interface MaterialJpaRepository extends JpaRepository<MaterialJpaEntity, Long> {
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
}

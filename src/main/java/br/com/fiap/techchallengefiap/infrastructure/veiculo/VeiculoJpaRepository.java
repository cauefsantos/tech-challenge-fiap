package br.com.fiap.techchallengefiap.infrastructure.veiculo;

import org.springframework.data.jpa.repository.JpaRepository;

interface VeiculoJpaRepository extends JpaRepository<VeiculoJpaEntity, Long> {
    boolean existsByPlacaAndAtivoTrue(String placa);
    boolean existsByPlacaAndAtivoTrueAndIdNot(String placa, Long id);
}

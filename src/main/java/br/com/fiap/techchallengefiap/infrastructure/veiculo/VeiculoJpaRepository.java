package br.com.fiap.techchallengefiap.infrastructure.veiculo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface VeiculoJpaRepository extends JpaRepository<VeiculoJpaEntity, Long> {
    Optional<VeiculoJpaEntity> findFirstByPlacaOrderByAtivoDescIdDesc(String placa);
    boolean existsByPlacaAndAtivoTrue(String placa);
    boolean existsByPlacaAndAtivoTrueAndIdNot(String placa, Long id);
}

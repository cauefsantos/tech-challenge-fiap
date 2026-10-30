package br.com.fiap.techchallengefiap.infrastructure.ordemservico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select os from OrdemServicoJpaEntity os where os.id = :id")
    Optional<OrdemServicoJpaEntity> buscarPorIdParaAtualizacao(@Param("id") Long id);
}

package br.com.fiap.techchallengefiap.infrastructure.material;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

interface MaterialJpaRepository extends JpaRepository<MaterialJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MaterialJpaEntity m where m.id = :id")
    Optional<MaterialJpaEntity> buscarPorIdParaAtualizacao(@Param("id") Long id);

    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
}

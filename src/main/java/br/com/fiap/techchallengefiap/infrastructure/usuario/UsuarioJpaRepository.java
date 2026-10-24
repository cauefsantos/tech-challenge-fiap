package br.com.fiap.techchallengefiap.infrastructure.usuario;

import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Page<UsuarioJpaEntity> findAllByOrderByIdAsc(Pageable pageable);
    Optional<UsuarioJpaEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<UsuarioJpaEntity> findByUsernameAndEnabledTrue(String username);
    long countByPapelAndEnabledTrue(Papel papel);
}

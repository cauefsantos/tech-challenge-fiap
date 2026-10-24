package br.com.fiap.techchallengefiap.infrastructure.usuario;

import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario")
class UsuarioJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, unique = true, length = 100)
    String username;
    @Column(name = "password_hash", nullable = false, length = 100)
    String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    Papel papel;
    @Column(nullable = false)
    boolean enabled;
    @Column(name = "cliente_id")
    Long clienteId;
    @Column(name = "created_at", nullable = false)
    OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    OffsetDateTime updatedAt;

    protected UsuarioJpaEntity() {}
}

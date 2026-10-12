package br.com.fiap.techchallengefiap.infrastructure.cliente;

import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "cliente")
class ClienteJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false, length = 10)
    TipoPessoa tipoPessoa;
    @Column(nullable = false, length = 14)
    String documento;
    @Column(nullable = false)
    String nome;
    String email;
    String telefone;
    @Column(name = "data_cadastro", nullable = false)
    OffsetDateTime dataCadastro;
    @Column(nullable = false)
    boolean ativo;

    protected ClienteJpaEntity() {}
}

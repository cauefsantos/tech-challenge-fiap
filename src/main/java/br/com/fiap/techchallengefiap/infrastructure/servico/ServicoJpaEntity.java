package br.com.fiap.techchallengefiap.infrastructure.servico;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "servico")
class ServicoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, unique = true)
    String codigo;
    @Column(nullable = false)
    String nome;
    String descricao;
    @Column(name = "valor_base", nullable = false, precision = 15, scale = 2)
    BigDecimal valorBase;
    @Column(name = "tempo_estimado_segundos", nullable = false)
    Long tempoEstimadoSegundos;
    @Column(name = "data_cadastro", nullable = false)
    OffsetDateTime dataCadastro;
    @Column(nullable = false)
    boolean ativo;

    protected ServicoJpaEntity() {}
}

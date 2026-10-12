package br.com.fiap.techchallengefiap.infrastructure.ordemservico;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_servico")
class ItemServicoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "ordem_servico_id", nullable = false)
    Long ordemServicoId;
    @Column(name = "servico_id", nullable = false)
    Long servicoId;
    @Column(nullable = false)
    String descricao;
    @Column(nullable = false, precision = 15, scale = 3)
    BigDecimal quantidade;
    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    BigDecimal valorUnitario;
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal subtotal;
    @Column(name = "tempo_estimado_segundos", nullable = false)
    Long tempoEstimadoSegundos;

    protected ItemServicoJpaEntity() {}
}

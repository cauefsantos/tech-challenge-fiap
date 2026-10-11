package br.com.fiap.oficina.infrastructure.persistence.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.model.StatusOrdemServico;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ordem_servico")
class OrdemServicoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "cliente_id", nullable = false)
    Long clienteId;
    @Column(name = "veiculo_id", nullable = false)
    Long veiculoId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StatusOrdemServico status;
    @Column(name = "orcamento_aprovado", nullable = false)
    boolean orcamentoAprovado;
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    BigDecimal valorTotal;
    @Column(name = "criada_em", nullable = false)
    OffsetDateTime criadaEm;

    protected OrdemServicoJpaEntity() {
    }
}

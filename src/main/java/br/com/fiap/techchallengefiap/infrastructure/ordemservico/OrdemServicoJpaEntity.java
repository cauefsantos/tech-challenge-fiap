package br.com.fiap.techchallengefiap.infrastructure.ordemservico;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
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
    @Column(nullable = false, unique = true, length = 32)
    String numero;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StatusOrdemServico status;
    @Column(name = "orcamento_aprovado", nullable = false)
    boolean orcamentoAprovado;
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    BigDecimal valorTotal;
    @Column(name = "criada_em", nullable = false)
    OffsetDateTime criadaEm;
    @Column(name = "descricao_problema")
    String descricaoProblema;
    String diagnostico;
    @Column(name = "data_inicio_execucao")
    OffsetDateTime dataInicioExecucao;
    @Column(name = "data_finalizacao")
    OffsetDateTime dataFinalizacao;
    @Column(name = "data_entrega")
    OffsetDateTime dataEntrega;
    String observacoes;

    protected OrdemServicoJpaEntity() {
    }
}

package br.com.fiap.techchallengefiap.infrastructure.orcamento;

import br.com.fiap.techchallengefiap.domain.orcamento.model.StatusOrcamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "orcamento")
class OrcamentoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "ordem_servico_id", nullable = false)
    Long ordemServicoId;
    @Column(nullable = false)
    Integer versao;
    @Column(name = "valor_servicos", nullable = false, precision = 15, scale = 2)
    BigDecimal valorServicos;
    @Column(name = "valor_pecas", nullable = false, precision = 15, scale = 2)
    BigDecimal valorPecas;
    @Column(name = "valor_insumos", nullable = false, precision = 15, scale = 2)
    BigDecimal valorInsumos;
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    BigDecimal valorTotal;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    StatusOrcamento status;
    @Column(name = "data_geracao", nullable = false)
    OffsetDateTime dataGeracao;
    @Column(name = "data_resposta")
    OffsetDateTime dataResposta;
    @Column(name = "observacao_resposta")
    String observacaoResposta;

    protected OrcamentoJpaEntity() {}
}

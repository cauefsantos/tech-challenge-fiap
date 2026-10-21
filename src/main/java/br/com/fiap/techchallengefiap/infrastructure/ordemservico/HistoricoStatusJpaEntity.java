package br.com.fiap.techchallengefiap.infrastructure.ordemservico;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "historico_status")
class HistoricoStatusJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "ordem_servico_id", nullable = false)
    Long ordemServicoId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 40)
    StatusOrdemServico statusAnterior;
    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 40)
    StatusOrdemServico statusNovo;
    @Column(name = "data_alteracao", nullable = false)
    OffsetDateTime dataAlteracao;
    String observacao;

    protected HistoricoStatusJpaEntity() {}
}

package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;

import java.time.OffsetDateTime;
import java.util.Objects;

public class HistoricoStatus {
    private final Long id;
    private final Long ordemServicoId;
    private final StatusOrdemServico statusAnterior;
    private final StatusOrdemServico statusNovo;
    private final OffsetDateTime dataAlteracao;
    private final String observacao;

    public HistoricoStatus(Long ordemServicoId, StatusOrdemServico statusAnterior,
                           StatusOrdemServico statusNovo, String observacao) {
        this(null, ordemServicoId, statusAnterior, statusNovo, OffsetDateTime.now(), observacao);
    }

    public HistoricoStatus(Long id, Long ordemServicoId, StatusOrdemServico statusAnterior,
                           StatusOrdemServico statusNovo, OffsetDateTime dataAlteracao, String observacao) {
        if (statusNovo == null) throw new DomainException("O novo status e obrigatorio.");
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.dataAlteracao = Objects.requireNonNull(dataAlteracao);
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }

    public void setId(Long id) { }
    public Long getId() { return id; }
    public Long getOrdemServicoId() { return ordemServicoId; }
    public StatusOrdemServico getStatusAnterior() { return statusAnterior; }
    public StatusOrdemServico getStatusNovo() { return statusNovo; }
    public OffsetDateTime getDataAlteracao() { return dataAlteracao; }
    public String getObservacao() { return observacao; }
}

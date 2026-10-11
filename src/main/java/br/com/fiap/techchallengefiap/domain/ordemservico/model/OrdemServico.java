package br.com.fiap.oficina.domain.ordemservico.model;

import br.com.fiap.oficina.domain.shared.DomainException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class OrdemServico {
    private Long id;
    private final Long clienteId;
    private final Long veiculoId;
    private StatusOrdemServico status;
    private boolean orcamentoAprovado;
    private BigDecimal valorTotal;
    private final OffsetDateTime criadaEm;

    public OrdemServico(Long clienteId, Long veiculoId) {
        if (clienteId == null || veiculoId == null) throw new DomainException("Cliente e veiculo sao obrigatorios.");
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.status = StatusOrdemServico.RECEBIDA;
        this.valorTotal = BigDecimal.ZERO;
        this.criadaEm = OffsetDateTime.now();
    }

    public OrdemServico(Long id, Long clienteId, Long veiculoId, StatusOrdemServico status, boolean aprovado, BigDecimal valorTotal, OffsetDateTime criadaEm) {
        this.id = id;
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.status = status;
        this.orcamentoAprovado = aprovado;
        this.valorTotal = valorTotal;
        this.criadaEm = criadaEm;
    }

    public void enviarParaDiagnostico() {
        transicionar(StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO);
    }

    public void aguardarAprovacao(BigDecimal valor) {
        if (status != StatusOrdemServico.EM_DIAGNOSTICO) throw new DomainException("A OS deve estar em diagnostico.");
        if (valor == null || valor.signum() <= 0) throw new DomainException("O orcamento deve ser positivo.");
        valorTotal = valor;
        status = StatusOrdemServico.AGUARDANDO_APROVACAO;
    }

    public void aprovarOrcamento() {
        transicionar(StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.EM_EXECUCAO);
        orcamentoAprovado = true;
    }

    public void finalizar() {
        if (!orcamentoAprovado) throw new DomainException("O orcamento precisa estar aprovado.");
        transicionar(StatusOrdemServico.EM_EXECUCAO, StatusOrdemServico.FINALIZADA);
    }

    public void entregar() {
        transicionar(StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE);
    }

    private void transicionar(StatusOrdemServico esperado, StatusOrdemServico novo) {
        if (status != esperado) throw new DomainException("Transicao de status invalida: " + status + " -> " + novo);
        status = novo;
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getVeiculoId() {
        return veiculoId;
    }

    public StatusOrdemServico getStatus() {
        return status;
    }

    public boolean isOrcamentoAprovado() {
        return orcamentoAprovado;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public OffsetDateTime getCriadaEm() {
        return criadaEm;
    }
}

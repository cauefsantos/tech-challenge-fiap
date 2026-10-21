package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.servico.model.Servico;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ItemServico {
    private Long id;
    private final Long ordemServicoId;
    private final Long servicoId;
    private final String descricao;
    private final BigDecimal quantidade;
    private final BigDecimal valorUnitario;
    private final BigDecimal subtotal;
    private final long tempoEstimadoSegundos;

    public ItemServico(Long ordemServicoId, Servico servico, BigDecimal quantidade) {
        if (servico == null || servico.getId() == null) throw new DomainException("O servico deve existir.");
        if (!servico.isAtivo()) throw new DomainException("Servicos inativos nao podem ser adicionados a novas ordens.");
        if (quantidade == null || quantidade.signum() <= 0) throw new DomainException("A quantidade deve ser maior que zero.");
        this.ordemServicoId = ordemServicoId;
        this.servicoId = servico.getId();
        this.descricao = servico.getNome();
        this.quantidade = quantidade;
        this.valorUnitario = servico.getValorBase();
        this.subtotal = quantidade.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
        this.tempoEstimadoSegundos = servico.getTempoEstimado().toSeconds();
    }

    public ItemServico(Long id, Long ordemServicoId, Long servicoId, String descricao, BigDecimal quantidade,
                       BigDecimal valorUnitario, BigDecimal subtotal, long tempoEstimadoSegundos) {
        if (servicoId == null || servicoId <= 0) throw new DomainException("O servico deve ser informado.");
        if (quantidade == null || quantidade.signum() <= 0) throw new DomainException("A quantidade deve ser maior que zero.");
        if (valorUnitario == null || valorUnitario.signum() < 0) throw new DomainException("O valor unitario nao pode ser negativo.");
        if (tempoEstimadoSegundos <= 0) throw new DomainException("O tempo estimado deve ser positivo.");
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.servicoId = servicoId;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.subtotal = quantidade.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
        if (subtotal != null && subtotal.compareTo(this.subtotal) != 0) throw new DomainException("O subtotal nao corresponde a quantidade e ao valor unitario.");
        this.tempoEstimadoSegundos = tempoEstimadoSegundos;
    }

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public Long getOrdemServicoId() { return ordemServicoId; }
    public Long getServicoId() { return servicoId; }
    public String getDescricao() { return descricao; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public long getTempoEstimadoSegundos() { return tempoEstimadoSegundos; }
}

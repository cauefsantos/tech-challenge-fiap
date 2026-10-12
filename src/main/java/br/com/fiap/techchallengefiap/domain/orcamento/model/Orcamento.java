package br.com.fiap.techchallengefiap.domain.orcamento.model;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemServico;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public class Orcamento {
    private Long id;
    private final Long ordemServicoId;
    private final int versao;
    private BigDecimal valorServicos;
    private BigDecimal valorPecas;
    private BigDecimal valorInsumos;
    private BigDecimal valorTotal;
    private StatusOrcamento status;
    private final OffsetDateTime dataGeracao;
    private OffsetDateTime dataResposta;
    private String observacaoResposta;

    public Orcamento(Long ordemServicoId, int versao, List<ItemServico> servicos, List<ItemMaterial> materiais) {
        if (ordemServicoId == null || ordemServicoId <= 0) throw new DomainException("A ordem de servico e obrigatoria.");
        if (versao <= 0) throw new DomainException("A versao do orcamento deve ser positiva.");
        this.ordemServicoId = ordemServicoId;
        this.versao = versao;
        this.status = StatusOrcamento.PENDENTE;
        this.dataGeracao = OffsetDateTime.now();
        atualizarValores(servicos, materiais);
    }

    public Orcamento(Long id, Long ordemServicoId, int versao, BigDecimal valorServicos, BigDecimal valorPecas,
                     BigDecimal valorInsumos, BigDecimal valorTotal, StatusOrcamento status,
                     OffsetDateTime dataGeracao, OffsetDateTime dataResposta, String observacaoResposta) {
        if (ordemServicoId == null || ordemServicoId <= 0 || versao <= 0) throw new DomainException("Ordem de servico e versao sao obrigatorias.");
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.versao = versao;
        this.valorServicos = naoNegativo(valorServicos);
        this.valorPecas = naoNegativo(valorPecas);
        this.valorInsumos = naoNegativo(valorInsumos);
        this.valorTotal = valorServicos.add(valorPecas).add(valorInsumos);
        if (valorTotal != null && valorTotal.compareTo(this.valorTotal) != 0) throw new DomainException("O valor total nao corresponde aos valores do orcamento.");
        this.status = Objects.requireNonNull(status);
        this.dataGeracao = Objects.requireNonNull(dataGeracao);
        this.dataResposta = dataResposta;
        this.observacaoResposta = observacaoResposta;
    }

    public void disponibilizar() {
        transicionar(StatusOrcamento.PENDENTE, StatusOrcamento.DISPONIBILIZADO);
    }

    public void aprovar(String observacao) {
        responder(StatusOrcamento.APROVADO, observacao);
    }

    public void rejeitar(String observacao) {
        responder(StatusOrcamento.REJEITADO, observacao);
    }

    public void cancelar() {
        if (status == StatusOrcamento.APROVADO || status == StatusOrcamento.CANCELADO) {
            throw new DomainException("Este orcamento nao pode ser cancelado.");
        }
        status = StatusOrcamento.CANCELADO;
    }

    public void atualizarValores(List<ItemServico> servicos, List<ItemMaterial> materiais) {
        if (status != StatusOrcamento.PENDENTE && status != StatusOrcamento.DISPONIBILIZADO) {
            throw new DomainException("Este orcamento nao pode ter seus valores alterados.");
        }
        if (servicos == null || materiais == null) throw new DomainException("Os itens do orcamento sao obrigatorios.");
        valorServicos = servicos.stream().map(ItemServico::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        valorPecas = materiais.stream()
                .filter(i -> i.getTipo() == br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial.PECA)
                .map(ItemMaterial::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        valorInsumos = materiais.stream()
                .filter(i -> i.getTipo() == br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial.INSUMO)
                .map(ItemMaterial::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        valorTotal = valorServicos.add(valorPecas).add(valorInsumos).setScale(2, RoundingMode.HALF_UP);
    }

    private void responder(StatusOrcamento resposta, String observacao) {
        if (status != StatusOrcamento.DISPONIBILIZADO) {
            throw new DomainException("Somente um orcamento disponibilizado pode ser aprovado ou rejeitado.");
        }
        status = resposta;
        dataResposta = OffsetDateTime.now();
        observacaoResposta = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }

    private void transicionar(StatusOrcamento esperado, StatusOrcamento novo) {
        if (status != esperado) throw new DomainException("Transicao de orcamento invalida: " + status + " -> " + novo);
        status = novo;
    }

    private static BigDecimal naoNegativo(BigDecimal valor) {
        if (valor == null || valor.signum() < 0) throw new DomainException("Os valores do orcamento nao podem ser negativos.");
        return valor;
    }

    public Long getId() { return id; }
    public Long getOrdemServicoId() { return ordemServicoId; }
    public int getVersao() { return versao; }
    public BigDecimal getValorServicos() { return valorServicos; }
    public BigDecimal getValorPecas() { return valorPecas; }
    public BigDecimal getValorInsumos() { return valorInsumos; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public StatusOrcamento getStatus() { return status; }
    public OffsetDateTime getDataGeracao() { return dataGeracao; }
    public OffsetDateTime getDataResposta() { return dataResposta; }
    public String getObservacaoResposta() { return observacaoResposta; }
}

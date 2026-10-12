package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.orcamento.model.Orcamento;
import br.com.fiap.techchallengefiap.domain.orcamento.model.StatusOrcamento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OrdemServico {
    private Long id;
    private final String numero;
    private final Long clienteId;
    private final Long veiculoId;
    private StatusOrdemServico status;
    private boolean orcamentoAprovado;
    private BigDecimal valorTotal;
    private final OffsetDateTime criadaEm;
    private String descricaoProblema;
    private String diagnostico;
    private OffsetDateTime dataInicioExecucao;
    private OffsetDateTime dataFinalizacao;
    private OffsetDateTime dataEntrega;
    private String observacoes;
    private final List<ItemServico> itensServico = new ArrayList<>();
    private final List<ItemMaterial> itensMaterial = new ArrayList<>();
    private final List<Orcamento> orcamentos = new ArrayList<>();
    private final List<HistoricoStatus> historicoStatus = new ArrayList<>();

    public OrdemServico(Long clienteId, Long veiculoId) {
        if (clienteId == null || clienteId <= 0 || veiculoId == null || veiculoId <= 0) {
            throw new DomainException("Cliente e veiculo sao obrigatorios.");
        }
        this.numero = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.status = StatusOrdemServico.RECEBIDA;
        this.valorTotal = BigDecimal.ZERO;
        this.criadaEm = OffsetDateTime.now();
    }

    public OrdemServico(Long id, Long clienteId, Long veiculoId, StatusOrdemServico status, boolean aprovado, BigDecimal valorTotal, OffsetDateTime criadaEm) {
        this(id, id == null ? UUID.randomUUID().toString().substring(0, 8).toUpperCase() : id.toString(),
                clienteId, veiculoId, status, aprovado, valorTotal, criadaEm, null, null, null, null, null, null);
    }

    public OrdemServico(Long id, String numero, Long clienteId, Long veiculoId, StatusOrdemServico status,
                        boolean aprovado, BigDecimal valorTotal, OffsetDateTime criadaEm,
                        String descricaoProblema, String diagnostico, OffsetDateTime dataInicioExecucao,
                        OffsetDateTime dataFinalizacao, OffsetDateTime dataEntrega, String observacoes) {
        this.id = id;
        this.numero = numero;
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.status = status;
        this.orcamentoAprovado = aprovado;
        this.valorTotal = valorTotal;
        this.criadaEm = criadaEm;
        this.descricaoProblema = descricaoProblema;
        this.diagnostico = diagnostico;
        this.dataInicioExecucao = dataInicioExecucao;
        this.dataFinalizacao = dataFinalizacao;
        this.dataEntrega = dataEntrega;
        this.observacoes = observacoes;
    }

    public void definirDescricaoProblema(String descricao) {
        verificarEdicao();
        if (descricao == null || descricao.isBlank()) throw new DomainException("A descricao do problema e obrigatoria.");
        descricaoProblema = descricao.trim();
    }

    public void registrarDiagnostico(String descricao) {
        verificarEdicao();
        if (status != StatusOrdemServico.EM_DIAGNOSTICO) throw new DomainException("A OS deve estar em diagnostico.");
        if (descricao == null || descricao.isBlank()) throw new DomainException("O diagnostico e obrigatorio.");
        diagnostico = descricao.trim();
    }

    public void adicionarItemServico(ItemServico item) {
        verificarEdicao();
        if (item == null) throw new DomainException("O item de servico e obrigatorio.");
        validarVinculo(item.getOrdemServicoId());
        itensServico.add(item);
        recalcularValorTotal();
    }

    public void adicionarItemMaterial(ItemMaterial item) {
        verificarEdicao();
        if (item == null) throw new DomainException("O item de material e obrigatorio.");
        validarVinculo(item.getOrdemServicoId());
        itensMaterial.add(item);
        recalcularValorTotal();
    }

    public void removerItemServico(Long itemId) {
        verificarEdicao();
        if (!itensServico.removeIf(item -> item.getId() != null && item.getId().equals(itemId))) {
            throw new DomainException("Item de servico nao encontrado.");
        }
        recalcularValorTotal();
    }

    public void removerItemMaterial(Long itemId) {
        verificarEdicao();
        if (!itensMaterial.removeIf(item -> item.getId() != null && item.getId().equals(itemId))) {
            throw new DomainException("Item de material nao encontrado.");
        }
        recalcularValorTotal();
    }

    public Orcamento gerarOrcamento() {
        verificarEdicao();
        if (status != StatusOrdemServico.EM_DIAGNOSTICO) throw new DomainException("A OS deve estar em diagnostico para gerar orcamento.");
        long versao = orcamentos.stream().mapToLong(Orcamento::getVersao).max().orElse(0) + 1;
        Orcamento orcamento = new Orcamento(id, (int) versao, itensServico, itensMaterial);
        orcamentos.add(orcamento);
        return orcamento;
    }

    public void disponibilizarOrcamento(Orcamento orcamento) {
        verificarEdicao();
        if (orcamento == null || !orcamentos.contains(orcamento)) throw new DomainException("O orcamento nao pertence a esta OS.");
        if (status != StatusOrdemServico.EM_DIAGNOSTICO) throw new DomainException("A OS deve estar em diagnostico.");
        if (orcamento.getValorTotal().signum() <= 0) throw new DomainException("O orcamento deve ser positivo.");
        orcamento.disponibilizar();
        aguardarAprovacao(orcamento.getValorTotal());
    }

    public void aprovarOrcamento(Long orcamentoId, String observacao) {
        Orcamento orcamento = orcamentos.stream()
                .filter(item -> item.getId() != null && item.getId().equals(orcamentoId))
                .findFirst().orElseThrow(() -> new DomainException("Orcamento nao encontrado nesta OS."));
        aprovarOrcamento(orcamento, observacao);
    }

    public void aprovarOrcamento(Orcamento orcamento, String observacao) {
        if (status != StatusOrdemServico.AGUARDANDO_APROVACAO) throw new DomainException("A OS deve estar aguardando aprovacao.");
        if (orcamento == null || !orcamentos.contains(orcamento)) throw new DomainException("O orcamento nao pertence a esta OS.");
        if (orcamentos.stream().anyMatch(item -> item.getStatus() == StatusOrcamento.APROVADO)) {
            throw new DomainException("Ja existe um orcamento aprovado para esta OS.");
        }
        orcamento.aprovar(observacao);
        aprovarOrcamento();
    }

    public void enviarParaDiagnostico() {
        transicionar(StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO);
    }

    public void aguardarAprovacao(BigDecimal valor) {
        if (status != StatusOrdemServico.EM_DIAGNOSTICO) throw new DomainException("A OS deve estar em diagnostico.");
        if (valor == null || valor.signum() <= 0) throw new DomainException("O orcamento deve ser positivo.");
        BigDecimal valorDosItens = itensServico.stream().map(ItemServico::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(itensMaterial.stream().map(ItemMaterial::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        if ((!itensServico.isEmpty() || !itensMaterial.isEmpty()) && valor.compareTo(valorDosItens) != 0) {
            throw new DomainException("O valor do orcamento deve corresponder aos itens da OS.");
        }
        valorTotal = valor;
        transicionar(StatusOrdemServico.EM_DIAGNOSTICO, StatusOrdemServico.AGUARDANDO_APROVACAO);
    }

    public void aprovarOrcamento() {
        if (status != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new DomainException("A OS deve estar aguardando aprovacao.");
        }
        orcamentoAprovado = true;
        iniciarExecucao();
    }

    public void iniciarExecucao() {
        if (!orcamentoAprovado) throw new DomainException("O orcamento precisa estar aprovado.");
        transicionar(StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.EM_EXECUCAO);
        dataInicioExecucao = OffsetDateTime.now();
    }

    public void finalizar() {
        if (!orcamentoAprovado) throw new DomainException("O orcamento precisa estar aprovado.");
        transicionar(StatusOrdemServico.EM_EXECUCAO, StatusOrdemServico.FINALIZADA);
        dataFinalizacao = OffsetDateTime.now();
    }

    public void entregar() {
        transicionar(StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE);
        dataEntrega = OffsetDateTime.now();
    }

    private void transicionar(StatusOrdemServico esperado, StatusOrdemServico novo) {
        if (status != esperado) throw new DomainException("Transicao de status invalida: " + status + " -> " + novo);
        StatusOrdemServico anterior = status;
        status = novo;
        historicoStatus.add(new HistoricoStatus(id, anterior, novo, null));
    }

    private void verificarEdicao() {
        if (status == StatusOrdemServico.ENTREGUE) throw new DomainException("A OS nao pode ser alterada depois de entregue.");
        if (orcamentoAprovado) throw new DomainException("Os itens nao podem ser alterados depois da aprovacao do orcamento.");
    }

    private void validarVinculo(Long itemOrdemServicoId) {
        if (itemOrdemServicoId != null && !itemOrdemServicoId.equals(id)) {
            throw new DomainException("O item pertence a outra ordem de servico.");
        }
    }

    private void recalcularValorTotal() {
        valorTotal = itensServico.stream().map(ItemServico::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(itensMaterial.stream().map(ItemMaterial::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        for (Orcamento orcamento : orcamentos) {
            if (orcamento.getStatus() == StatusOrcamento.PENDENTE
                    || orcamento.getStatus() == StatusOrcamento.DISPONIBILIZADO) {
                orcamento.atualizarValores(itensServico, itensMaterial);
            }
        }
    }

    public void definirObservacoes(String observacoes) {
        verificarEdicao();
        this.observacoes = observacoes == null || observacoes.isBlank() ? null : observacoes.trim();
    }

    public Long getId() {
        return id;
    }

    public String getNumero() { return numero; }
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

    public String getDescricaoProblema() { return descricaoProblema; }
    public String getDiagnostico() { return diagnostico; }
    public OffsetDateTime getDataInicioExecucao() { return dataInicioExecucao; }
    public OffsetDateTime getDataFinalizacao() { return dataFinalizacao; }
    public OffsetDateTime getDataEntrega() { return dataEntrega; }
    public String getObservacoes() { return observacoes; }
    public List<ItemServico> getItensServico() { return Collections.unmodifiableList(itensServico); }
    public List<ItemMaterial> getItensMaterial() { return Collections.unmodifiableList(itensMaterial); }
    public List<Orcamento> getOrcamentos() { return Collections.unmodifiableList(orcamentos); }
    public List<HistoricoStatus> getHistoricoStatus() { return Collections.unmodifiableList(historicoStatus); }
}

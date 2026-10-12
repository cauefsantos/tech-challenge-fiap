package br.com.fiap.techchallengefiap.domain.servico.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Servico {
    private Long id;
    private final String codigo;
    private final String nome;
    private final String descricao;
    private final BigDecimal valorBase;
    private final Duration tempoEstimado;
    private final OffsetDateTime dataCadastro;
    private boolean ativo;

    public Servico(String codigo, String nome, String descricao, BigDecimal valorBase, Duration tempoEstimado) {
        this(null, codigo, nome, descricao, valorBase, tempoEstimado, OffsetDateTime.now(), true);
    }

    public Servico(Long id, String codigo, String nome, String descricao, BigDecimal valorBase,
                   Duration tempoEstimado, OffsetDateTime dataCadastro, boolean ativo) {
        this.codigo = obrigatorio(codigo, "O codigo do servico e obrigatorio.");
        this.nome = obrigatorio(nome, "O nome do servico e obrigatorio.");
        this.descricao = descricao == null ? null : descricao.trim();
        this.valorBase = naoNegativo(valorBase, "O valor base nao pode ser negativo.");
        if (tempoEstimado == null || tempoEstimado.isNegative() || tempoEstimado.isZero()) {
            throw new DomainException("O tempo estimado deve ser positivo.");
        }
        this.tempoEstimado = tempoEstimado;
        this.id = id;
        this.dataCadastro = Objects.requireNonNull(dataCadastro, "A data de cadastro e obrigatoria.");
        this.ativo = ativo;
    }

    public void desativar() { ativo = false; }
    public void ativar() { ativo = true; }

    private static BigDecimal naoNegativo(BigDecimal valor, String mensagem) {
        if (valor == null || valor.signum() < 0) throw new DomainException(mensagem);
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static String obrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) throw new DomainException(mensagem);
        return texto.trim();
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorBase() { return valorBase; }
    public Duration getTempoEstimado() { return tempoEstimado; }
    public OffsetDateTime getDataCadastro() { return dataCadastro; }
    public boolean isAtivo() { return ativo; }
}

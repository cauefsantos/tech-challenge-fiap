package br.com.fiap.techchallengefiap.domain.material.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Material {
    private Long id;
    private final String codigo;
    private final TipoMaterial tipo;
    private final String nome;
    private final String descricao;
    private final BigDecimal valorUnitario;
    private final String unidadeMedida;
    private BigDecimal quantidadeEstoque;
    private final BigDecimal estoqueMinimo;
    private final OffsetDateTime dataCadastro;
    private boolean ativo;

    public Material(String codigo, TipoMaterial tipo, String nome, String descricao, BigDecimal valorUnitario,
                    String unidadeMedida, BigDecimal quantidadeEstoque, BigDecimal estoqueMinimo) {
        this(null, codigo, tipo, nome, descricao, valorUnitario, unidadeMedida, quantidadeEstoque,
                estoqueMinimo, OffsetDateTime.now(), true);
    }

    public Material(Long id, String codigo, TipoMaterial tipo, String nome, String descricao,
                    BigDecimal valorUnitario, String unidadeMedida, BigDecimal quantidadeEstoque,
                    BigDecimal estoqueMinimo, OffsetDateTime dataCadastro, boolean ativo) {
        this.codigo = obrigatorio(codigo, "O codigo do material e obrigatorio.");
        this.tipo = Objects.requireNonNull(tipo, "O tipo do material e obrigatorio.");
        this.nome = obrigatorio(nome, "O nome do material e obrigatorio.");
        this.descricao = descricao == null ? null : descricao.trim();
        this.valorUnitario = naoNegativo(valorUnitario, "O valor unitario nao pode ser negativo.")
                .setScale(2, RoundingMode.HALF_UP);
        this.unidadeMedida = obrigatorio(unidadeMedida, "A unidade de medida e obrigatoria.");
        this.quantidadeEstoque = naoNegativo(quantidadeEstoque, "A quantidade em estoque nao pode ser negativa.");
        this.estoqueMinimo = naoNegativo(estoqueMinimo, "O estoque minimo nao pode ser negativo.");
        this.id = id;
        this.dataCadastro = Objects.requireNonNull(dataCadastro, "A data de cadastro e obrigatoria.");
        this.ativo = ativo;
    }

    public void validarDisponibilidade(BigDecimal quantidade) {
        BigDecimal solicitada = positiva(quantidade, "A quantidade deve ser maior que zero.");
        if (!ativo) throw new DomainException("Materiais inativos nao podem ser usados em novas ordens de servico.");
        if (quantidadeEstoque.compareTo(solicitada) < 0) throw new DomainException("Estoque insuficiente para o material.");
    }

    public void consumir(BigDecimal quantidade) {
        validarDisponibilidade(quantidade);
        quantidadeEstoque = quantidadeEstoque.subtract(quantidade);
    }

    public void desativar() { ativo = false; }
    public void ativar() { ativo = true; }

    private static BigDecimal positiva(BigDecimal valor, String mensagem) {
        if (valor == null || valor.signum() <= 0) throw new DomainException(mensagem);
        return valor;
    }

    private static BigDecimal naoNegativo(BigDecimal valor, String mensagem) {
        if (valor == null || valor.signum() < 0) throw new DomainException(mensagem);
        return valor;
    }

    private static String obrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) throw new DomainException(mensagem);
        return texto.trim();
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public TipoMaterial getTipo() { return tipo; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public String getUnidadeMedida() { return unidadeMedida; }
    public BigDecimal getQuantidadeEstoque() { return quantidadeEstoque; }
    public BigDecimal getEstoqueMinimo() { return estoqueMinimo; }
    public OffsetDateTime getDataCadastro() { return dataCadastro; }
    public boolean isAtivo() { return ativo; }
}

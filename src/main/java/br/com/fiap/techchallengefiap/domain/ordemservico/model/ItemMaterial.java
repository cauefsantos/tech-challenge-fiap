package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ItemMaterial {
    private Long id;
    private final Long ordemServicoId;
    private final Long materialId;
    private final TipoMaterial tipo;
    private final String descricao;
    private final BigDecimal quantidade;
    private final BigDecimal valorUnitario;
    private final BigDecimal subtotal;

    public ItemMaterial(Long ordemServicoId, Material material, BigDecimal quantidade) {
        if (material == null || material.getId() == null) throw new DomainException("O material deve existir.");
        material.validarDisponibilidade(quantidade);
        this.ordemServicoId = ordemServicoId;
        this.materialId = material.getId();
        this.tipo = material.getTipo();
        this.descricao = material.getNome();
        this.quantidade = quantidade;
        this.valorUnitario = material.getValorUnitario();
        this.subtotal = quantidade.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
    }

    public ItemMaterial(Long id, Long ordemServicoId, Long materialId, TipoMaterial tipo, String descricao,
                        BigDecimal quantidade, BigDecimal valorUnitario, BigDecimal subtotal) {
        if (materialId == null || materialId <= 0) throw new DomainException("O material deve ser informado.");
        if (tipo == null) throw new DomainException("O tipo do material e obrigatorio.");
        if (quantidade == null || quantidade.signum() <= 0) throw new DomainException("A quantidade deve ser maior que zero.");
        if (valorUnitario == null || valorUnitario.signum() < 0) throw new DomainException("O valor unitario nao pode ser negativo.");
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.materialId = materialId;
        this.tipo = tipo;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.subtotal = quantidade.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
        if (subtotal != null && subtotal.compareTo(this.subtotal) != 0) throw new DomainException("O subtotal nao corresponde a quantidade e ao valor unitario.");
    }

    public void consumir(Material material) {
        if (material == null || !materialId.equals(material.getId())) throw new DomainException("O material nao corresponde ao item.");
        material.consumir(quantidade);
    }

    public Long getId() { return id; }
    public Long getOrdemServicoId() { return ordemServicoId; }
    public Long getMaterialId() { return materialId; }
    public TipoMaterial getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}

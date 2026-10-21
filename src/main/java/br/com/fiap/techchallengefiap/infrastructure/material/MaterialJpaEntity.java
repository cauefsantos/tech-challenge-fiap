package br.com.fiap.techchallengefiap.infrastructure.material;

import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "material")
class MaterialJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, unique = true)
    String codigo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    TipoMaterial tipo;
    @Column(nullable = false)
    String nome;
    String descricao;
    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    BigDecimal valorUnitario;
    @Column(name = "unidade_medida", nullable = false)
    String unidadeMedida;
    @Column(name = "quantidade_estoque", nullable = false, precision = 15, scale = 3)
    BigDecimal quantidadeEstoque;
    @Column(name = "estoque_minimo", nullable = false, precision = 15, scale = 3)
    BigDecimal estoqueMinimo;
    @Column(name = "data_cadastro", nullable = false)
    OffsetDateTime dataCadastro;
    @Column(nullable = false)
    boolean ativo;

    protected MaterialJpaEntity() {}
}

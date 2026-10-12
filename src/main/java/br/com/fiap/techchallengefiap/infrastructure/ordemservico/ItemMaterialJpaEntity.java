package br.com.fiap.techchallengefiap.infrastructure.ordemservico;

import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_material")
class ItemMaterialJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "ordem_servico_id", nullable = false)
    Long ordemServicoId;
    @Column(name = "material_id", nullable = false)
    Long materialId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    TipoMaterial tipo;
    @Column(nullable = false)
    String descricao;
    @Column(nullable = false, precision = 15, scale = 3)
    BigDecimal quantidade;
    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    BigDecimal valorUnitario;
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal subtotal;

    protected ItemMaterialJpaEntity() {}
}

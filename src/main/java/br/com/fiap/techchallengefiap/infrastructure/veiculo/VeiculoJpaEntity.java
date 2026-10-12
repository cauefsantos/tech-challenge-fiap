package br.com.fiap.techchallengefiap.infrastructure.veiculo;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "veiculo")
class VeiculoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "cliente_id", nullable = false)
    Long clienteId;
    @Column(nullable = false, length = 7)
    String placa;
    @Column(nullable = false)
    String marca;
    @Column(nullable = false)
    String modelo;
    @Column(nullable = false)
    Integer ano;
    @Column(name = "data_cadastro", nullable = false)
    OffsetDateTime dataCadastro;
    @Column(nullable = false)
    boolean ativo;

    protected VeiculoJpaEntity() {}
}

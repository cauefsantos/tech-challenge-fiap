package br.com.fiap.techchallengefiap.domain.veiculo.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Veiculo {
    private Long id;
    private final Long clienteId;
    private final String placa;
    private final String marca;
    private final String modelo;
    private final Integer ano;
    private final OffsetDateTime dataCadastro;
    private boolean ativo;

    public Veiculo(Long clienteId, String placa, String marca, String modelo, Integer ano) {
        this(null, clienteId, placa, marca, modelo, ano, OffsetDateTime.now(), true);
    }

    public Veiculo(Long id, Long clienteId, String placa, String marca, String modelo, Integer ano,
                   OffsetDateTime dataCadastro, boolean ativo) {
        if (clienteId == null || clienteId <= 0) throw new DomainException("O cliente do veiculo e obrigatorio.");
        String placaNormalizada = normalizarPlaca(placa);
        if (!placaNormalizada.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}")) {
            throw new DomainException("A placa do veiculo nao e valida.");
        }
        this.clienteId = clienteId;
        this.placa = placaNormalizada;
        this.marca = obrigatorio(marca, "A marca e obrigatoria.");
        this.modelo = obrigatorio(modelo, "O modelo e obrigatorio.");
        if (ano == null || ano < 1886 || ano > OffsetDateTime.now().getYear() + 1) {
            throw new DomainException("O ano do veiculo nao e valido.");
        }
        this.ano = ano;
        this.id = id;
        this.dataCadastro = Objects.requireNonNull(dataCadastro, "A data de cadastro e obrigatoria.");
        this.ativo = ativo;
    }

    public void desativar() { ativo = false; }
    public void ativar() { ativo = true; }

    public static String normalizarPlaca(String placa) {
        if (placa == null) throw new DomainException("A placa e obrigatoria.");
        return placa.replace("-", "").replace(" ", "").toUpperCase();
    }

    private static String obrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) throw new DomainException(mensagem);
        return texto.trim();
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public String getPlaca() { return placa; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public Integer getAno() { return ano; }
    public OffsetDateTime getDataCadastro() { return dataCadastro; }
    public boolean isAtivo() { return ativo; }
}

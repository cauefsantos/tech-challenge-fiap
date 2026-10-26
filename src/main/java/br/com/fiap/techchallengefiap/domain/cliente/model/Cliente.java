package br.com.fiap.techchallengefiap.domain.cliente.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Cliente {
    private Long id;
    private final TipoPessoa tipoPessoa;
    private final String documento;
    private final String nome;
    private final String email;
    private final String telefone;
    private final OffsetDateTime dataCadastro;
    private boolean ativo;

    public Cliente(TipoPessoa tipoPessoa, String documento, String nome, String email, String telefone) {
        this(null, tipoPessoa, documento, nome, email, telefone, OffsetDateTime.now(), true);
    }

    public Cliente(Long id, TipoPessoa tipoPessoa, String documento, String nome, String email,
                   String telefone, OffsetDateTime dataCadastro, boolean ativo) {
        if (tipoPessoa == null) throw new DomainException("O tipo de pessoa e obrigatorio.");
        this.tipoPessoa = tipoPessoa;
        this.documento = normalizarDocumento(documento);
        validarDocumento(tipoPessoa, this.documento);
        this.nome = textoObrigatorio(nome, "O nome e obrigatorio.");
        this.email = validarEmail(email);
        this.telefone = telefone == null || telefone.isBlank() ? null : telefone.trim();
        this.id = id;
        this.dataCadastro = Objects.requireNonNull(dataCadastro, "A data de cadastro e obrigatoria.");
        this.ativo = ativo;
    }

    public void desativar() {
        ativo = false;
    }

    public void ativar() {
        ativo = true;
    }

    public static String normalizarDocumento(String valor) {
        if (valor == null) throw new DomainException("O documento e obrigatorio.");
        String documento = valor.trim();
        if (!documento.matches("[0-9.\\-/\\s]+")) throw new DomainException("O documento deve conter apenas digitos e formatacao.");
        return documento.replaceAll("\\D", "");
    }

    private static void validarDocumento(TipoPessoa tipo, String documento) {
        boolean valido = tipo == TipoPessoa.FISICA ? validarCpf(documento) : validarCnpj(documento);
        if (!valido) throw new DomainException("O documento nao e valido para o tipo de pessoa.");
    }

    private static boolean validarCpf(String cpf) {
        if (!cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) return false;
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
        int digito = (soma * 10) % 11;
        if (digito == 10) digito = 0;
        if (digito != cpf.charAt(9) - '0') return false;
        soma = 0;
        for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
        digito = (soma * 10) % 11;
        if (digito == 10) digito = 0;
        return digito == cpf.charAt(10) - '0';
    }

    private static boolean validarCnpj(String cnpj) {
        if (!cnpj.matches("\\d{14}") || cnpj.chars().distinct().count() == 1) return false;
        int[] pesosPrimeiro = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesosSegundo = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int primeiro = calcularDigito(cnpj, pesosPrimeiro);
        int segundo = calcularDigito(cnpj, pesosSegundo);
        return primeiro == cnpj.charAt(12) - '0' && segundo == cnpj.charAt(13) - '0';
    }

    private static int calcularDigito(String documento, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) soma += (documento.charAt(i) - '0') * pesos[i];
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static String validarEmail(String email) {
        if (email == null || email.isBlank()) return null;
        String valor = email.trim();
        if (!valor.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new DomainException("O email nao e valido.");
        }
        return valor;
    }

    private static String textoObrigatorio(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) throw new DomainException(mensagem);
        return texto.trim();
    }

    public Long getId() { return id; }
    public TipoPessoa getTipoPessoa() { return tipoPessoa; }
    public String getDocumento() { return documento; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public OffsetDateTime getDataCadastro() { return dataCadastro; }
    public boolean isAtivo() { return ativo; }
}

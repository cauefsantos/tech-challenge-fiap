package br.com.fiap.techchallengefiap.domain.usuario.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

public class Usuario {
    private final Long id;
    private final String username;
    private final String passwordHash;
    private Papel papel;
    private boolean enabled;
    private Long clienteId;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Usuario(Long id, String username, String passwordHash, Papel papel, boolean enabled,
                   Long clienteId, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.username = normalizarUsername(username);
        if (passwordHash == null || passwordHash.isBlank()) throw new DomainException("O hash da senha e obrigatorio.");
        this.passwordHash = passwordHash;
        this.papel = Objects.requireNonNull(papel, "O papel e obrigatorio.");
        validarVinculo(papel, clienteId);
        this.enabled = enabled;
        this.clienteId = clienteId;
        this.createdAt = Objects.requireNonNull(createdAt, "A data de criacao e obrigatoria.");
        this.updatedAt = Objects.requireNonNull(updatedAt, "A data de atualizacao e obrigatoria.");
    }

    public static String normalizarUsername(String username) {
        if (username == null || username.isBlank()) throw new DomainException("O username e obrigatorio.");
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 100 || !normalized.matches("[a-z0-9._@+-]+")) {
            throw new DomainException("O username deve ter ate 100 caracteres e conter apenas letras, numeros, '.', '_', '@', '+' ou '-'.");
        }
        return normalized;
    }

    public void atualizar(Papel papel, Long clienteId, boolean enabled) {
        Objects.requireNonNull(papel, "O papel e obrigatorio.");
        validarVinculo(papel, clienteId);
        this.papel = papel;
        this.clienteId = clienteId;
        this.enabled = enabled;
        this.updatedAt = OffsetDateTime.now();
    }

    public void desativar() {
        this.enabled = false;
        this.updatedAt = OffsetDateTime.now();
    }

    private static void validarVinculo(Papel papel, Long clienteId) {
        if (papel == Papel.CLIENTE && clienteId == null) {
            throw new DomainException("Usuarios CLIENTE devem estar vinculados a um cliente.");
        }
        if (papel != Papel.CLIENTE && clienteId != null) {
            throw new DomainException("Somente usuarios CLIENTE podem estar vinculados a um cliente.");
        }
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Papel getPapel() { return papel; }
    public boolean isEnabled() { return enabled; }
    public Long getClienteId() { return clienteId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}

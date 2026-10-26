package br.com.fiap.techchallengefiap.presentation.rest.usuario;

import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import jakarta.validation.constraints.NotNull;

public record AtualizarUsuarioRequest(@NotNull Papel papel, String documentoCliente, @NotNull Boolean enabled) {}

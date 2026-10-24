package br.com.fiap.techchallengefiap.presentation.rest.usuario;

import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotNull Papel papel,
        Long clienteId
) {}

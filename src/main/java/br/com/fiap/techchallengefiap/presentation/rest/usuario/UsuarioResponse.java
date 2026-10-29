package br.com.fiap.techchallengefiap.presentation.rest.usuario;

import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;

import java.time.OffsetDateTime;

public record UsuarioResponse(
        Long id,
        String username,
        Papel papel,
        boolean enabled,
        String documentoCliente,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static UsuarioResponse from(Usuario usuario, String documentoCliente) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getPapel(), usuario.isEnabled(),
                documentoCliente, usuario.getCreatedAt(), usuario.getUpdatedAt());
    }
}

package br.com.fiap.techchallengefiap.presentation.rest.usuario;

import java.util.List;

public record PaginaUsuarioResponse(
        List<UsuarioResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}

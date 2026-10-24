package br.com.fiap.techchallengefiap.domain.usuario.model;

import java.util.List;

public record Pagina<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}

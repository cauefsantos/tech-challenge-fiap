package br.com.fiap.techchallengefiap.presentation.rest.cliente;

import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClienteRequest(
        @NotNull TipoPessoa tipoPessoa,
        @NotBlank String documento,
        @NotBlank String nome,
        String email,
        String telefone) {
}

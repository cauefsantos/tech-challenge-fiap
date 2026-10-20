package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import jakarta.validation.constraints.NotBlank;

public record DiagnosticoRequest(@NotBlank String diagnostico) {
}

package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuscarOrdemServicoUseCaseTest {
    @Mock OrdemServicoRepository repository;
    @InjectMocks BuscarOrdemServicoUseCase useCase;

    @Test
    void retornaOsQuandoEncontrada() {
        OrdemServico os = OrdemServicoUseCaseFixture.os(br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico.RECEBIDA);
        when(repository.buscarPorId(10L)).thenReturn(Optional.of(os));
        assertSame(os, useCase.executar(10L));
    }

    @Test
    void informaOsNaoEncontrada() {
        when(repository.buscarPorId(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.executar(10L));
    }
}

package br.com.fiap.techchallengefiap.application.servico;

import br.com.fiap.techchallengefiap.domain.servico.model.Servico;
import br.com.fiap.techchallengefiap.domain.servico.repository.ServicoRepository;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GerenciarServicoUseCaseTest {
    @Mock ServicoRepository repository;
    @InjectMocks GerenciarServicoUseCase useCase;

    @Test
    void criaServicoComTempoEmSegundos() {
        when(repository.existeCodigo("SVC-1")).thenReturn(false);
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        Servico salvo = useCase.criar("SVC-1", "Reparo", null, new BigDecimal("10.00"), 3600L);
        assertEquals(Duration.ofSeconds(3600), salvo.getTempoEstimado());
        verify(repository).salvar(any(Servico.class));
    }

    @Test
    void rejeitaTempoEstimadoAusente() {
        assertThrows(DomainException.class, () -> useCase.criar("S", "N", null, BigDecimal.TEN, null));
        verify(repository, never()).salvar(any());
    }

    @Test
    void impedeCodigoDuplicadoNaCriacaoOuAtualizacao() {
        when(repository.existeCodigo("SVC-1")).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.criar("SVC-1", "N", null, BigDecimal.TEN, 60L));
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(servico(1L, true)));
        when(repository.existeCodigoParaOutroServico("SVC-2", 1L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.atualizar(1L, "SVC-2", "N", null, BigDecimal.TEN, 60L));
        verify(repository, never()).salvar(any());
    }

    @Test
    void buscaListaEInformaServicoNaoEncontrado() {
        Servico servico = servico(1L, true);
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(repository.buscarPorId(2L)).thenReturn(Optional.empty());
        when(repository.listar()).thenReturn(List.of(servico));
        assertSame(servico, useCase.buscar(1L));
        assertEquals(List.of(servico), useCase.listar());
        assertThrows(ResourceNotFoundException.class, () -> useCase.buscar(2L));
    }

    @Test
    void atualizaServicoPreservandoIdDataCadastroEAtivacao() {
        Servico existente = servico(1L, true);
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(existente));
        when(repository.existeCodigoParaOutroServico("SVC-2", 1L)).thenReturn(false);
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        Servico atualizado = useCase.atualizar(1L, "SVC-2", "Novo reparo", null, BigDecimal.ONE, 120L);
        assertEquals(1L, atualizado.getId());
        assertEquals(existente.getDataCadastro(), atualizado.getDataCadastro());
        assertTrue(atualizado.isAtivo());
        assertEquals(Duration.ofSeconds(120), atualizado.getTempoEstimado());
        verify(repository).salvar(atualizado);
    }

    @Test
    void alteraAtivacaoDoServico() {
        Servico servico = servico(1L, true);
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertFalse(useCase.alterarAtivo(1L, false).isAtivo());
        assertTrue(useCase.alterarAtivo(1L, true).isAtivo());
        verify(repository, times(2)).salvar(servico);
    }

    private Servico servico(Long id, boolean ativo) { return new Servico(id, "SVC-1", "Reparo", null, BigDecimal.TEN, Duration.ofMinutes(30), OffsetDateTime.parse("2024-01-01T00:00Z"), ativo); }
}

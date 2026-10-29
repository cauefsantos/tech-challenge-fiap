package br.com.fiap.techchallengefiap.application.material;

import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.material.repository.MaterialRepository;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GerenciarMaterialUseCaseTest {
    @Mock MaterialRepository repository;
    @InjectMocks GerenciarMaterialUseCase useCase;

    @Test
    void criaMaterialQuandoCodigoAindaNaoEstaEmUso() {
        when(repository.existeCodigo("M-1")).thenReturn(false);
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        Material salvo = useCase.criar("M-1", TipoMaterial.PECA, "Filtro", null, BigDecimal.TEN, "un", BigDecimal.ONE, BigDecimal.ZERO);
        assertEquals("M-1", salvo.getCodigo());
        verify(repository).salvar(any(Material.class));
    }

    @Test
    void impedeCodigoDuplicadoNaCriacaoOuAtualizacao() {
        when(repository.existeCodigo("M-1")).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.criar("M-1", TipoMaterial.PECA, "Filtro", null, BigDecimal.TEN, "un", BigDecimal.ONE, BigDecimal.ZERO));
        when(repository.buscarPorId(3L)).thenReturn(Optional.of(material(3L, true)));
        when(repository.existeCodigoParaOutroMaterial("M-2", 3L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.atualizar(3L, "M-2", TipoMaterial.PECA, "Filtro", null, BigDecimal.TEN, "un", BigDecimal.ONE, BigDecimal.ZERO));
        verify(repository, never()).salvar(any());
    }

    @Test
    void buscaListaEInformaMaterialNaoEncontrado() {
        Material material = material(3L, true);
        when(repository.buscarPorId(3L)).thenReturn(Optional.of(material));
        when(repository.buscarPorId(4L)).thenReturn(Optional.empty());
        when(repository.listar()).thenReturn(List.of(material));
        assertSame(material, useCase.buscar(3L));
        assertEquals(List.of(material), useCase.listar());
        assertThrows(ResourceNotFoundException.class, () -> useCase.buscar(4L));
    }

    @Test
    void atualizaPreservandoDadosDeCadastro() {
        Material existente = material(3L, true);
        when(repository.buscarPorId(3L)).thenReturn(Optional.of(existente));
        when(repository.existeCodigoParaOutroMaterial("M-2", 3L)).thenReturn(false);
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        Material atualizado = useCase.atualizar(3L, "M-2", TipoMaterial.INSUMO, "Novo", "desc", BigDecimal.ONE, "kg", BigDecimal.TEN, BigDecimal.ONE);
        assertEquals(3L, atualizado.getId());
        assertEquals(existente.getDataCadastro(), atualizado.getDataCadastro());
        assertTrue(atualizado.isAtivo());
        assertEquals("M-2", atualizado.getCodigo());
        verify(repository).salvar(atualizado);
    }

    @Test
    void alteraAtivacaoDoMaterial() {
        Material material = material(3L, true);
        when(repository.buscarPorId(3L)).thenReturn(Optional.of(material));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertFalse(useCase.alterarAtivo(3L, false).isAtivo());
        assertTrue(useCase.alterarAtivo(3L, true).isAtivo());
        verify(repository, times(2)).salvar(material);
    }

    private Material material(Long id, boolean ativo) { return new Material(id, "M-1", TipoMaterial.PECA, "Filtro", null, BigDecimal.TEN, "un", BigDecimal.ONE, BigDecimal.ZERO, OffsetDateTime.parse("2024-01-01T00:00Z"), ativo); }
}

package br.com.fiap.techchallengefiap.application.veiculo;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;
import br.com.fiap.techchallengefiap.domain.veiculo.repository.VeiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GerenciarVeiculoUseCaseTest {
    @Mock VeiculoRepository repository;
    @Mock ClienteRepository clienteRepository;
    @InjectMocks GerenciarVeiculoUseCase useCase;

    @Test
    void criaVeiculoParaClienteAtivo() {
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(12L, true)));
        when(repository.existePlacaAtiva("ABC1D23")).thenReturn(false);
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        Veiculo salvo = useCase.criar("529.982.247-25", "abc-1d23", "Marca", "Modelo", 2024);
        assertEquals("ABC1D23", salvo.getPlaca());
        assertEquals(12L, salvo.getClienteId());
        verify(repository).salvar(any(Veiculo.class));
    }

    @Test
    void impedeCriacaoParaClienteInexistenteOuInativo() {
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.criar("52998224725", "ABC1D23", "M", "X", 2024));
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(12L, false)));
        assertThrows(DomainException.class, () -> useCase.criar("52998224725", "ABC1D23", "M", "X", 2024));
        verify(repository, never()).salvar(any());
    }

    @Test
    void impedePlacaAtivaDuplicada() {
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(12L, true)));
        when(repository.existePlacaAtiva("ABC1D23")).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.criar("52998224725", "ABC1D23", "M", "X", 2024));
        verify(repository, never()).salvar(any());
    }

    @Test
    void buscaListaEAtualizaVeiculo() {
        Veiculo existente = veiculo(21L, 12L, "ABC1D23", true);
        when(repository.buscarPorPlaca("ABC1D23")).thenReturn(Optional.of(existente));
        when(repository.listar()).thenReturn(List.of(existente));
        when(clienteRepository.buscarPorDocumento("39053344705")).thenReturn(Optional.of(cliente(14L, true)));
        when(repository.existePlacaAtivaParaOutroVeiculo("DEF1G23", 21L)).thenReturn(false);
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertSame(existente, useCase.buscar("abc-1d23"));
        assertEquals(List.of(existente), useCase.listar());
        Veiculo atualizado = useCase.atualizar("ABC1D23", "39053344705", "DEF1G23", "Nova", "Linha", 2023);
        assertEquals(21L, atualizado.getId());
        assertEquals(14L, atualizado.getClienteId());
        assertEquals(existente.getDataCadastro(), atualizado.getDataCadastro());
        assertTrue(atualizado.isAtivo());
        verify(repository).salvar(atualizado);
    }

    @Test
    void impedeAtualizacaoComPlacaDeOutroVeiculo() {
        when(repository.buscarPorPlaca("ABC1D23")).thenReturn(Optional.of(veiculo(21L, 12L, "ABC1D23", true)));
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(12L, true)));
        when(repository.existePlacaAtivaParaOutroVeiculo("DEF1G23", 21L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.atualizar("ABC1D23", "52998224725", "DEF1G23", "M", "X", 2024));
        verify(repository, never()).salvar(any());
    }

    @Test
    void desativaVeiculo() {
        Veiculo veiculo = veiculo(21L, 12L, "ABC1D23", true);
        when(repository.buscarPorPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertFalse(useCase.alterarAtivo("ABC1D23", false).isAtivo());
        verify(repository).salvar(veiculo);
    }

    @Test
    void impedeReativacaoComClienteInativoOuPlacaEmUso() {
        Veiculo veiculo = veiculo(21L, 12L, "ABC1D23", false);
        when(repository.buscarPorPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));
        when(clienteRepository.buscarPorId(12L)).thenReturn(Optional.of(cliente(12L, false)));
        assertThrows(DomainException.class, () -> useCase.alterarAtivo("ABC1D23", true));
        when(clienteRepository.buscarPorId(12L)).thenReturn(Optional.of(cliente(12L, true)));
        when(repository.existePlacaAtivaParaOutroVeiculo("ABC1D23", 21L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.alterarAtivo("ABC1D23", true));
        verify(repository, never()).salvar(any());
    }

    @Test
    void informaAusenciasNaBuscaEDocumentoDoCliente() {
        when(repository.buscarPorPlaca("ABC1D23")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.buscar("ABC1D23"));
        when(clienteRepository.buscarPorId(12L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.documentoDoCliente(veiculo(21L, 12L, "ABC1D23", true)));
    }

    @Test
    void retornaDocumentoDoCliente() {
        when(clienteRepository.buscarPorId(12L)).thenReturn(Optional.of(cliente(12L, true)));
        assertEquals("52998224725", useCase.documentoDoCliente(veiculo(21L, 12L, "ABC1D23", true)));
    }

    private Cliente cliente(Long id, boolean ativo) { return new Cliente(id, TipoPessoa.FISICA, "52998224725", "Cliente", null, null, OffsetDateTime.parse("2024-01-01T00:00Z"), ativo); }
    private Veiculo veiculo(Long id, Long clienteId, String placa, boolean ativo) { return new Veiculo(id, clienteId, placa, "Marca", "Modelo", 2022, OffsetDateTime.parse("2024-01-01T00:00Z"), ativo); }
}

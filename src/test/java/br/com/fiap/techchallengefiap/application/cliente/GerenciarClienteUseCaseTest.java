package br.com.fiap.techchallengefiap.application.cliente;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
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
class GerenciarClienteUseCaseTest {
    @Mock ClienteRepository repository;
    @InjectMocks GerenciarClienteUseCase useCase;

    @Test
    void criaClienteQuandoDocumentoAindaNaoEstaEmUso() {
        when(repository.existeDocumentoAtivo("52998224725")).thenReturn(false);
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        Cliente salvo = useCase.criar(TipoPessoa.FISICA, "529.982.247-25", " Cliente ", null, null);
        assertEquals("52998224725", salvo.getDocumento());
        assertEquals("Cliente", salvo.getNome());
        verify(repository).existeDocumentoAtivo("52998224725");
        verify(repository).salvar(any(Cliente.class));
    }

    @Test
    void impedeCriacaoComDocumentoAtivoDuplicado() {
        when(repository.existeDocumentoAtivo("52998224725")).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.criar(TipoPessoa.FISICA, "52998224725", "Cliente", null, null));
        verify(repository, never()).salvar(any());
    }

    @Test
    void buscaOuInformaClienteNaoEncontrado() {
        when(repository.buscarPorDocumento("52998224725")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.buscar("529.982.247-25"));
        verify(repository).buscarPorDocumento("52998224725");
    }

    @Test
    void listaClientesDoRepositorio() {
        List<Cliente> clientes = List.of(cliente(1L, "52998224725", true));
        when(repository.listar()).thenReturn(clientes);
        assertSame(clientes, useCase.listar());
    }

    @Test
    void atualizaPreservandoIdDataEAtivacao() {
        Cliente existente = cliente(7L, "52998224725", true);
        when(repository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(existente));
        when(repository.existeDocumentoAtivoParaOutroCliente("39053344705", 7L)).thenReturn(false);
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        Cliente atualizado = useCase.atualizar("52998224725", TipoPessoa.FISICA, "39053344705", "Novo", null, null);
        assertEquals(7L, atualizado.getId());
        assertEquals(existente.getDataCadastro(), atualizado.getDataCadastro());
        assertTrue(atualizado.isAtivo());
        assertEquals("39053344705", atualizado.getDocumento());
        verify(repository).salvar(atualizado);
    }

    @Test
    void impedeAtualizacaoComDocumentoDeOutroCliente() {
        when(repository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(7L, "52998224725", true)));
        when(repository.existeDocumentoAtivoParaOutroCliente("39053344705", 7L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.atualizar("52998224725", TipoPessoa.FISICA, "39053344705", "Novo", null, null));
        verify(repository, never()).salvar(any());
    }

    @Test
    void desativaCliente() {
        Cliente existente = cliente(7L, "52998224725", true);
        when(repository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(existente));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertFalse(useCase.alterarAtivo("52998224725", false).isAtivo());
        verify(repository).salvar(existente);
    }

    @Test
    void impedeReativacaoComDocumentoEmUso() {
        when(repository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(7L, "52998224725", false)));
        when(repository.existeDocumentoAtivoParaOutroCliente("52998224725", 7L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.alterarAtivo("52998224725", true));
        verify(repository, never()).salvar(any());
    }

    private Cliente cliente(Long id, String documento, boolean ativo) {
        return new Cliente(id, TipoPessoa.FISICA, documento, "Cliente", null, null, OffsetDateTime.parse("2024-01-01T10:00:00Z"), ativo);
    }
}

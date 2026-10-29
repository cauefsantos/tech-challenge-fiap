package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
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
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarOrdemServicoUseCaseTest {
    @Mock OrdemServicoRepository repository;
    @Mock ClienteRepository clienteRepository;
    @Mock VeiculoRepository veiculoRepository;
    @InjectMocks CriarOrdemServicoUseCase useCase;

    @Test
    void criaOsParaClienteEVeiculoAtivosEVinculados() {
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(true)));
        when(veiculoRepository.buscarPorPlaca("ABC1D23")).thenReturn(Optional.of(veiculo(20L, true)));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        OrdemServico criada = useCase.executar(new CriarOrdemServicoCommand("529.982.247-25", "abc-1d23"));
        assertEquals(20L, criada.getClienteId());
        assertEquals(30L, criada.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, criada.getStatus());
        verify(repository).salvar(any(OrdemServico.class));
    }

    @Test
    void impedeCriacaoQuandoClienteNaoExisteOuEstaInativo() {
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.executar(comando()));
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(false)));
        assertThrows(DomainException.class, () -> useCase.executar(comando()));
        verify(repository, never()).salvar(any());
        verifyNoInteractions(veiculoRepository);
    }

    @Test
    void impedeCriacaoQuandoVeiculoNaoExisteOuEstaInativo() {
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(true)));
        when(veiculoRepository.buscarPorPlaca("ABC1D23")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.executar(comando()));
        when(veiculoRepository.buscarPorPlaca("ABC1D23")).thenReturn(Optional.of(veiculo(20L, false)));
        assertThrows(DomainException.class, () -> useCase.executar(comando()));
        verify(repository, never()).salvar(any());
    }

    @Test
    void impedeCriacaoQuandoVeiculoNaoPertenceAoCliente() {
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(true)));
        when(veiculoRepository.buscarPorPlaca("ABC1D23")).thenReturn(Optional.of(veiculo(99L, true)));
        assertThrows(DomainException.class, () -> useCase.executar(comando()));
        verify(repository, never()).salvar(any());
    }

    @Test
    void naoSalvaQuandoPrecondicaoFalha() {
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente(true)));
        when(veiculoRepository.buscarPorPlaca("ABC1D23")).thenReturn(Optional.of(veiculo(99L, true)));
        assertThrows(DomainException.class, () -> useCase.executar(comando()));
        verify(repository, never()).salvar(any());
    }

    private CriarOrdemServicoCommand comando() { return new CriarOrdemServicoCommand("52998224725", "ABC1D23"); }
    private Cliente cliente(boolean ativo) { return new Cliente(20L, TipoPessoa.FISICA, "52998224725", "Cliente", null, null, OffsetDateTime.parse("2024-01-01T00:00Z"), ativo); }
    private Veiculo veiculo(Long clienteId, boolean ativo) { return new Veiculo(30L, clienteId, "ABC1D23", "Marca", "Modelo", 2024, OffsetDateTime.parse("2024-01-01T00:00Z"), ativo); }
}

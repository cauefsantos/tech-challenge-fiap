package br.com.fiap.techchallengefiap.application;

import br.com.fiap.techchallengefiap.application.cliente.GerenciarClienteUseCase;
import br.com.fiap.techchallengefiap.application.veiculo.GerenciarVeiculoUseCase;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GerenciarEntidadesUseCaseTest {
    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private GerenciarClienteUseCase gerenciarCliente;

    @Test
    void criaClienteQuandoDocumentoAindaNaoEstaEmUso() {
        when(clienteRepository.existeDocumentoAtivo("52998224725")).thenReturn(false);
        when(clienteRepository.salvar(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente cliente = gerenciarCliente.criar(TipoPessoa.FISICA, "529.982.247-25", "Cliente", null, null);

        assertEquals("52998224725", cliente.getDocumento());
        verify(clienteRepository).salvar(any(Cliente.class));
    }

    @Test
    void impedeCriacaoDeClienteComDocumentoAtivoDuplicado() {
        when(clienteRepository.existeDocumentoAtivo("52998224725")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> gerenciarCliente.criar(TipoPessoa.FISICA, "52998224725", "Cliente", null, null));

        verify(clienteRepository, never()).salvar(any(Cliente.class));
    }

    @Test
    void impedeCriacaoDeVeiculoParaClienteInexistente() {
        GerenciarVeiculoUseCase gerenciarVeiculo = new GerenciarVeiculoUseCase(veiculoRepository, clienteRepository);
        when(clienteRepository.buscarPorDocumento("52998224725")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gerenciarVeiculo.criar("529.982.247-25", "ABC1D23", "Marca", "Modelo", 2022));

        verify(veiculoRepository, never()).salvar(any(Veiculo.class));
    }

    @Test
    void exigeQueClienteDaOrdemEstejaVinculadoAoVeiculo() {
        GerenciarVeiculoUseCase gerenciarVeiculo = new GerenciarVeiculoUseCase(veiculoRepository, clienteRepository);
        when(clienteRepository.buscarPorDocumento("52998224725"))
                .thenReturn(Optional.of(new Cliente(12L, TipoPessoa.FISICA, "52998224725", "Cliente",
                        null, null, java.time.OffsetDateTime.now(), true)));
        when(veiculoRepository.buscarPorPlaca("ABC1D23"))
                .thenReturn(Optional.of(new Veiculo(21L, 13L, "ABC1D23", "Marca", "Modelo", 2022,
                        java.time.OffsetDateTime.now(), true)));

        assertThrows(DomainException.class, () -> {
            var useCase = new br.com.fiap.techchallengefiap.application.ordemservico.CriarOrdemServicoUseCase(
                    mock(br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository.class),
                    clienteRepository, veiculoRepository);
            useCase.executar(new br.com.fiap.techchallengefiap.application.ordemservico.CriarOrdemServicoCommand("52998224725", "ABC1D23"));
        });
    }
}

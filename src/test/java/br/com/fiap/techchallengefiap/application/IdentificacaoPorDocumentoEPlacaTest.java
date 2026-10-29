package br.com.fiap.techchallengefiap.application;

import br.com.fiap.techchallengefiap.application.cliente.GerenciarClienteUseCase;
import br.com.fiap.techchallengefiap.application.ordemservico.CriarOrdemServicoCommand;
import br.com.fiap.techchallengefiap.application.ordemservico.CriarOrdemServicoUseCase;
import br.com.fiap.techchallengefiap.application.ordemservico.IdentificarOrdemServicoUseCase;
import br.com.fiap.techchallengefiap.application.usuario.GerenciarUsuarioUseCase;
import br.com.fiap.techchallengefiap.application.veiculo.GerenciarVeiculoUseCase;
import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;
import br.com.fiap.techchallengefiap.domain.usuario.repository.UsuarioRepository;
import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;
import br.com.fiap.techchallengefiap.domain.veiculo.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentificacaoPorDocumentoEPlacaTest {
    private static final String CPF = "52998224725";
    private static final String PLACA = "ABC1D23";

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private OrdemServicoRepository ordemServicoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private CriarOrdemServicoUseCase criarOrdemServico;
    private GerenciarClienteUseCase gerenciarCliente;
    private GerenciarVeiculoUseCase gerenciarVeiculo;
    private GerenciarUsuarioUseCase gerenciarUsuario;

    @BeforeEach
    void setUp() {
        criarOrdemServico = new CriarOrdemServicoUseCase(ordemServicoRepository, clienteRepository, veiculoRepository);
        gerenciarCliente = new GerenciarClienteUseCase(clienteRepository);
        gerenciarVeiculo = new GerenciarVeiculoUseCase(veiculoRepository, clienteRepository);
        gerenciarUsuario = new GerenciarUsuarioUseCase(usuarioRepository, clienteRepository, passwordEncoder);
    }

    @Test
    void criaOrdemDeServicoAPartirDoDocumentoEDaPlaca() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.of(cliente(12L, true)));
        when(veiculoRepository.buscarPorPlaca(PLACA)).thenReturn(Optional.of(veiculo(21L, 12L, true)));
        when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServico os = criarOrdemServico.executar(new CriarOrdemServicoCommand("529.982.247-25", "abc-1d23"));

        ArgumentCaptor<OrdemServico> salva = ArgumentCaptor.forClass(OrdemServico.class);
        verify(ordemServicoRepository).salvar(salva.capture());
        assertEquals(12L, salva.getValue().getClienteId());
        assertEquals(21L, salva.getValue().getVeiculoId());
        assertEquals("RECEBIDA", os.getStatus().name());
    }

    @Test
    void naoCriaOrdemDeServicoParaDocumentoNaoCadastrado() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> criarOrdemServico.executar(new CriarOrdemServicoCommand(CPF, PLACA)));
        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void naoCriaOrdemDeServicoParaClienteInativo() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.of(cliente(12L, false)));

        assertThrows(DomainException.class,
                () -> criarOrdemServico.executar(new CriarOrdemServicoCommand(CPF, PLACA)));
        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void naoCriaOrdemDeServicoParaPlacaNaoCadastrada() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.of(cliente(12L, true)));
        when(veiculoRepository.buscarPorPlaca(PLACA)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> criarOrdemServico.executar(new CriarOrdemServicoCommand(CPF, PLACA)));
        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void naoCriaOrdemDeServicoParaVeiculoInativo() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.of(cliente(12L, true)));
        when(veiculoRepository.buscarPorPlaca(PLACA)).thenReturn(Optional.of(veiculo(21L, 12L, false)));

        assertThrows(DomainException.class,
                () -> criarOrdemServico.executar(new CriarOrdemServicoCommand(CPF, PLACA)));
        verify(ordemServicoRepository, never()).salvar(any());
    }

    @Test
    void buscaClientePeloDocumentoFormatado() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.of(cliente(12L, true)));

        assertEquals(CPF, gerenciarCliente.buscar("529.982.247-25").getDocumento());
    }

    @Test
    void informaClienteNaoEncontradoPorDocumento() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gerenciarCliente.buscar(CPF));
    }

    @Test
    void desativaClientePeloDocumento() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.of(cliente(12L, true)));
        when(clienteRepository.salvar(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertFalse(gerenciarCliente.alterarAtivo(CPF, false).isAtivo());
    }

    @Test
    void cadastraVeiculoVinculadoAoClientePeloDocumento() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.of(cliente(12L, true)));
        when(veiculoRepository.existePlacaAtiva(PLACA)).thenReturn(false);
        when(veiculoRepository.salvar(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo veiculo = gerenciarVeiculo.criar("529.982.247-25", "abc-1d23", "Fiat", "Uno", 2020);

        assertEquals(12L, veiculo.getClienteId());
        assertEquals(PLACA, veiculo.getPlaca());
    }

    @Test
    void naoCadastraVeiculoParaClienteInativo() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.of(cliente(12L, false)));

        assertThrows(DomainException.class, () -> gerenciarVeiculo.criar(CPF, PLACA, "Fiat", "Uno", 2020));
        verify(veiculoRepository, never()).salvar(any());
    }

    @Test
    void buscaVeiculoPelaPlacaEInformaODocumentoDoDono() {
        when(veiculoRepository.buscarPorPlaca(PLACA)).thenReturn(Optional.of(veiculo(21L, 12L, true)));
        when(clienteRepository.buscarPorId(12L)).thenReturn(Optional.of(cliente(12L, true)));

        Veiculo veiculo = gerenciarVeiculo.buscar("abc 1d23");

        assertEquals(PLACA, veiculo.getPlaca());
        assertEquals(CPF, gerenciarVeiculo.documentoDoCliente(veiculo));
    }

    @Test
    void identificaOrdemDeServicoPorDocumentoEPlaca() {
        when(clienteRepository.buscarPorId(12L)).thenReturn(Optional.of(cliente(12L, true)));
        when(veiculoRepository.buscarPorId(21L)).thenReturn(Optional.of(veiculo(21L, 12L, true)));

        var identificacao = new IdentificarOrdemServicoUseCase(clienteRepository, veiculoRepository)
                .executar(new OrdemServico(12L, 21L));

        assertEquals(CPF, identificacao.documentoCliente());
        assertEquals(PLACA, identificacao.placa());
    }

    @Test
    void vinculaUsuarioClienteAoClientePeloDocumento() {
        when(clienteRepository.buscarPorDocumento(CPF)).thenReturn(Optional.of(cliente(12L, true)));
        when(usuarioRepository.existeUsername("cliente.teste")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.salvar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clienteRepository.buscarPorId(12L)).thenReturn(Optional.of(cliente(12L, true)));

        Usuario usuario = gerenciarUsuario.criar("cliente.teste", "senha-de-teste-123", Papel.CLIENTE, "529.982.247-25");

        assertEquals(12L, usuario.getClienteId());
        assertEquals(CPF, gerenciarUsuario.documentoDoCliente(usuario));
    }

    @Test
    void recusaDocumentoDeClienteParaUsuarioQueNaoECliente() {
        assertThrows(IllegalArgumentException.class,
                () -> gerenciarUsuario.criar("func.teste", "senha-de-teste-123", Papel.FUNCIONARIO, CPF));
        verify(usuarioRepository, never()).salvar(any());
    }

    @Test
    void usuarioSemClienteNaoTemDocumento() {
        Usuario funcionario = new Usuario(1L, "func.teste", "hash", Papel.FUNCIONARIO, true, null,
                OffsetDateTime.now(), OffsetDateTime.now());

        assertNull(gerenciarUsuario.documentoDoCliente(funcionario));
    }

    private static Cliente cliente(Long id, boolean ativo) {
        return new Cliente(id, TipoPessoa.FISICA, CPF, "Cliente", null, null, OffsetDateTime.now(), ativo);
    }

    private static Veiculo veiculo(Long id, Long clienteId, boolean ativo) {
        return new Veiculo(id, clienteId, PLACA, "Fiat", "Uno", 2020, OffsetDateTime.now(), ativo);
    }
}

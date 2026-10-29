package br.com.fiap.techchallengefiap.application.usuario;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import br.com.fiap.techchallengefiap.domain.usuario.model.Pagina;
import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;
import br.com.fiap.techchallengefiap.domain.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GerenciarUsuarioUseCaseTest {
    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private ClienteRepository clientes;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GerenciarUsuarioUseCase useCase;

    @Test
    void criaUsuarioComUsernameNormalizadoESenhaCodificada() {
        when(usuarios.existeUsername("novo.usuario")).thenReturn(false);
        when(passwordEncoder.encode("senha-segura")).thenReturn("hash");
        when(usuarios.salvar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario criado = useCase.criar(" Novo.Usuario ", "senha-segura", Papel.FUNCIONARIO, null);

        assertEquals("novo.usuario", criado.getUsername());
        assertEquals("hash", criado.getPasswordHash());
        assertTrue(criado.isEnabled());
        assertNull(criado.getClienteId());
        verify(passwordEncoder).encode("senha-segura");
        verify(usuarios).salvar(criado);
    }

    @Test
    void criaUsuarioClienteComVinculoResolvidoPeloDocumento() {
        Cliente cliente = new Cliente(12L, TipoPessoa.FISICA, "52998224725", "Cliente",
                null, null, OffsetDateTime.now(), true);
        when(clientes.buscarPorDocumento("52998224725")).thenReturn(Optional.of(cliente));
        when(usuarios.existeUsername("cliente")).thenReturn(false);
        when(passwordEncoder.encode("senha")).thenReturn("hash");
        when(usuarios.salvar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario criado = useCase.criar("cliente", "senha", Papel.CLIENTE, "529.982.247-25");

        assertEquals(12L, criado.getClienteId());
        verify(usuarios).salvar(criado);
    }

    @Test
    void rejeitaUsernameDuplicadoESenhaComMaisDeSetentaEDoisBytesSemSalvar() {
        when(usuarios.existeUsername("duplicado")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> useCase.criar("duplicado", "senha", Papel.ADMIN, null));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.criar("outro", "a".repeat(73), Papel.ADMIN, null));

        verify(usuarios, never()).salvar(any());
    }

    @Test
    void exigeVinculoClienteERejeitaVinculoEmOutrosPapeis() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.criar("cliente", "senha", Papel.CLIENTE, null));
        assertThrows(IllegalArgumentException.class,
                () -> useCase.criar("admin", "senha", Papel.ADMIN, "52998224725"));
        verify(usuarios, never()).salvar(any());
    }

    @Test
    void informaUsuarioOuClienteVinculadoNaoEncontrados() {
        when(usuarios.buscarPorId(4L)).thenReturn(Optional.empty());
        when(clientes.buscarPorDocumento("52998224725")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.buscar(4L));
        assertThrows(ResourceNotFoundException.class,
                () -> useCase.criar("cliente", "senha", Papel.CLIENTE, "52998224725"));

        verify(usuarios, never()).salvar(any());
    }

    @Test
    void validaPaginacaoAntesDeDelegarAoRepositorio() {
        Pagina<Usuario> pagina = new Pagina<>(List.of(), 0, 10, 0, 0);
        when(usuarios.listar(0, 10)).thenReturn(pagina);

        assertSame(pagina, useCase.listar(0, 10));
        assertThrows(IllegalArgumentException.class, () -> useCase.listar(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> useCase.listar(0, 0));
        assertThrows(IllegalArgumentException.class, () -> useCase.listar(0, 101));
        verify(usuarios, times(1)).listar(0, 10);
    }

    @Test
    void atualizaPapelEAtivacaoDeUsuarioEncontrado() {
        Usuario atual = usuario(10L, Papel.FUNCIONARIO, true, null);
        when(usuarios.buscarPorId(10L)).thenReturn(Optional.of(atual));
        when(usuarios.salvar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario atualizado = useCase.atualizar(10L, Papel.ADMIN, null, false);

        assertSame(atual, atualizado);
        assertEquals(Papel.ADMIN, atualizado.getPapel());
        assertFalse(atualizado.isEnabled());
        verify(usuarios).salvar(atual);
    }

    @Test
    void impedeRebaixarOuDesativarUltimoAdministrador() {
        Usuario admin = usuario(1L, Papel.ADMIN, true, null);
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(admin));
        when(usuarios.contarAtivosPorPapel(Papel.ADMIN)).thenReturn(1L);

        assertThrows(ConflictException.class,
                () -> useCase.atualizar(1L, Papel.FUNCIONARIO, null, true));
        assertThrows(ConflictException.class, () -> useCase.desativar(1L));

        verify(usuarios, never()).salvar(any());
    }

    @Test
    void desativaUsuarioNaoAdministrador() {
        Usuario funcionario = usuario(2L, Papel.FUNCIONARIO, true, null);
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(funcionario));
        when(usuarios.salvar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario desativado = useCase.desativar(2L);

        assertFalse(desativado.isEnabled());
        verify(usuarios).salvar(funcionario);
    }

    @Test
    void consultaDocumentoDoClienteVinculadoSemExporConsultaParaUsuarioSemVinculo() {
        Usuario clienteVinculado = usuario(3L, Papel.CLIENTE, true, 12L);
        Cliente cliente = new Cliente(12L, TipoPessoa.FISICA, "52998224725", "Cliente",
                null, null, OffsetDateTime.now(), true);
        when(clientes.buscarPorId(12L)).thenReturn(Optional.of(cliente));

        assertEquals("52998224725", useCase.documentoDoCliente(clienteVinculado));
        assertNull(useCase.documentoDoCliente(usuario(4L, Papel.ADMIN, true, null)));
        verify(clientes).buscarPorId(12L);
    }

    @Test
    void rejeitaAtualizacaoComVinculoInexistenteSemSalvar() {
        Usuario funcionario = usuario(5L, Papel.FUNCIONARIO, true, null);
        when(usuarios.buscarPorId(5L)).thenReturn(Optional.of(funcionario));
        when(clientes.buscarPorDocumento("52998224725")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.atualizar(5L, Papel.CLIENTE, "52998224725", true));
        verify(usuarios, never()).salvar(any());
    }

    private Usuario usuario(Long id, Papel papel, boolean enabled, Long clienteId) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Usuario(id, "usuario." + id, "hash", papel, enabled, clienteId, now, now);
    }
}

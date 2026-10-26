package br.com.fiap.techchallengefiap.application.usuario;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;
import br.com.fiap.techchallengefiap.domain.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.nio.charset.StandardCharsets;
import br.com.fiap.techchallengefiap.domain.usuario.model.Pagina;

@Service
public class GerenciarUsuarioUseCase {
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final PasswordEncoder passwordEncoder;

    public GerenciarUsuarioUseCase(UsuarioRepository usuarios, ClienteRepository clientes, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario criar(String username, String password, Papel papel, String documentoCliente) {
        String normalized = Usuario.normalizarUsername(username);
        Long clienteId = resolverCliente(papel, documentoCliente);
        if (password == null || password.isBlank()) throw new IllegalArgumentException("A senha e obrigatoria.");
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("A senha deve ter no maximo 72 bytes UTF-8.");
        }
        if (usuarios.existeUsername(normalized)) throw new ConflictException("Ja existe um usuario com este username.");
        OffsetDateTime now = OffsetDateTime.now();
        return usuarios.salvar(new Usuario(null, normalized, passwordEncoder.encode(password), papel, true, clienteId, now, now));
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return usuarios.buscarPorId(id).orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado."));
    }

    @Transactional(readOnly = true)
    public Pagina<Usuario> listar(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("A pagina deve ser >= 0 e o tamanho deve estar entre 1 e 100.");
        }
        return usuarios.listar(page, size);
    }

    @Transactional
    public Usuario atualizar(Long id, Papel papel, String documentoCliente, boolean enabled) {
        Usuario atual = buscar(id);
        Long clienteId = resolverCliente(papel, documentoCliente);
        if (atual.isEnabled() && atual.getPapel() == Papel.ADMIN
                && (!enabled || papel != Papel.ADMIN)
                && usuarios.contarAtivosPorPapel(Papel.ADMIN) <= 1) {
            throw new ConflictException("Nao e permitido desativar ou rebaixar o ultimo ADMIN ativo.");
        }
        atual.atualizar(papel, clienteId, enabled);
        return usuarios.salvar(atual);
    }

    @Transactional
    public Usuario desativar(Long id) {
        Usuario usuario = buscar(id);
        if (usuario.isEnabled() && usuario.getPapel() == Papel.ADMIN
                && usuarios.contarAtivosPorPapel(Papel.ADMIN) <= 1) {
            throw new ConflictException("Nao e permitido desativar o ultimo ADMIN ativo.");
        }
        usuario.desativar();
        return usuarios.salvar(usuario);
    }

    /** CPF/CNPJ do cliente vinculado ao usuario, ou null para usuarios que nao sao CLIENTE. */
    @Transactional(readOnly = true)
    public String documentoDoCliente(Usuario usuario) {
        if (usuario.getClienteId() == null) return null;
        return clientes.buscarPorId(usuario.getClienteId())
                .map(Cliente::getDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente vinculado ao usuario nao encontrado."));
    }

    /** Valida o vinculo exigido pelo papel e devolve o id interno do cliente informado por CPF/CNPJ. */
    private Long resolverCliente(Papel papel, String documentoCliente) {
        if (papel == null) throw new IllegalArgumentException("O papel e obrigatorio.");
        boolean informado = documentoCliente != null && !documentoCliente.isBlank();
        if (papel == Papel.CLIENTE) {
            if (!informado) {
                throw new IllegalArgumentException("Usuarios CLIENTE devem estar vinculados a um cliente.");
            }
            return clientes.buscarPorDocumento(Cliente.normalizarDocumento(documentoCliente))
                    .map(Cliente::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente informado nao encontrado."));
        }
        if (informado) {
            throw new IllegalArgumentException("Somente usuarios CLIENTE podem estar vinculados a um cliente.");
        }
        return null;
    }
}

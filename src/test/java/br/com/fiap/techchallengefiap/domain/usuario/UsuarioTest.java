package br.com.fiap.techchallengefiap.domain.usuario;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {
    @Test
    void normalizaUsernameSemDiferenciarMaiusculas() {
        assertEquals("nome.sobrenome", Usuario.normalizarUsername("  Nome.Sobrenome  "));
    }

    @Test
    void exigeClienteParaPapelCliente() {
        assertThrows(DomainException.class, () -> usuario(Papel.CLIENTE, null));
    }

    @Test
    void impedeVinculoDeClienteEmOutrosPapeis() {
        assertThrows(DomainException.class, () -> usuario(Papel.ADMIN, 10L));
        assertThrows(DomainException.class, () -> usuario(Papel.FUNCIONARIO, 10L));
    }

    @Test
    void desativarPreservaUsuarioEAlteraEstado() {
        Usuario usuario = usuario(Papel.ADMIN, null);
        usuario.desativar();
        assertFalse(usuario.isEnabled());
        assertEquals("alice", usuario.getUsername());
        assertEquals("$2a$hash", usuario.getPasswordHash());
    }

    private Usuario usuario(Papel papel, Long clienteId) {
        var now = OffsetDateTime.now();
        return new Usuario(1L, "Alice", "$2a$hash", papel, true, clienteId, now, now);
    }
}

package br.com.fiap.techchallengefiap.infrastructure.security;

import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;
import br.com.fiap.techchallengefiap.domain.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminBootstrapTest {
    @Test
    void createsOnlyOnceAndNeverReplacesExistingCredentials() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        var now = OffsetDateTime.now();
        var existing = new Usuario(3L, "initial-admin", "$2a$existing-hash", Papel.ADMIN,
                true, null, now, now);
        when(repository.buscarPorUsername("initial-admin")).thenReturn(Optional.empty(), Optional.of(existing));
        when(encoder.encode("initial-admin-password")).thenReturn("$2a$new-hash");
        when(repository.salvar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var bootstrap = new AdminBootstrap(new AdminProperties("Initial-Admin", "initial-admin-password"),
                repository, encoder);
        var arguments = new DefaultApplicationArguments();
        bootstrap.run(arguments);
        bootstrap.run(arguments);

        verify(repository, times(1)).salvar(any(Usuario.class));
        verify(encoder, times(1)).encode("initial-admin-password");
        verify(repository, times(2)).buscarPorUsername("initial-admin");
    }
}

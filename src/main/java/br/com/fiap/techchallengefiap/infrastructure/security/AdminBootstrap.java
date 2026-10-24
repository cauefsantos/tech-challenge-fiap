package br.com.fiap.techchallengefiap.infrastructure.security;

import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;
import br.com.fiap.techchallengefiap.domain.usuario.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.nio.charset.StandardCharsets;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final AdminProperties properties;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(AdminProperties properties, UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("ADMIN_PASSWORD deve ter no maximo 72 bytes UTF-8.");
        }
        String username = Usuario.normalizarUsername(properties.username());
        if (usuarios.buscarPorUsername(username).isPresent()) return;
        OffsetDateTime now = OffsetDateTime.now();
        try {
            usuarios.salvar(new Usuario(null, username, passwordEncoder.encode(properties.password()), Papel.ADMIN,
                    true, null, now, now));
        } catch (DataIntegrityViolationException conflict) {
            if (usuarios.buscarPorUsername(username).isEmpty()) throw conflict;
        }
    }
}

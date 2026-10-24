package br.com.fiap.techchallengefiap.infrastructure.security;

import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;
import br.com.fiap.techchallengefiap.domain.usuario.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarios.buscarPorUsername(username)
                .filter(Usuario::isEnabled)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario ou senha invalidos."));
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .roles(usuario.getPapel().name())
                .disabled(!usuario.isEnabled())
                .build();
    }
}

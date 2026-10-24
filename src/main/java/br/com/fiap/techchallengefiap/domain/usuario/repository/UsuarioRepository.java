package br.com.fiap.techchallengefiap.domain.usuario.repository;

import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import br.com.fiap.techchallengefiap.domain.usuario.model.Pagina;
import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository {
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorUsername(String username);
    Pagina<Usuario> listar(int page, int size);
    long contarAtivosPorPapel(Papel papel);
    boolean existeUsername(String username);
}

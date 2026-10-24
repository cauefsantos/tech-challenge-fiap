package br.com.fiap.techchallengefiap.infrastructure.usuario;

import br.com.fiap.techchallengefiap.domain.usuario.model.Papel;
import br.com.fiap.techchallengefiap.domain.usuario.model.Pagina;
import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;
import br.com.fiap.techchallengefiap.domain.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {
    private final UsuarioJpaRepository jpa;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return toDomain(jpa.save(toEntity(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return jpa.findByUsername(Usuario.normalizarUsername(username)).map(this::toDomain);
    }

    @Override
    public Pagina<Usuario> listar(int page, int size) {
        var result = jpa.findAllByOrderByIdAsc(PageRequest.of(page, size)).map(this::toDomain);
        return new Pagina<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public long contarAtivosPorPapel(Papel papel) {
        return jpa.countByPapelAndEnabledTrue(papel);
    }

    @Override
    public boolean existeUsername(String username) {
        return jpa.existsByUsername(Usuario.normalizarUsername(username));
    }

    private UsuarioJpaEntity toEntity(Usuario usuario) {
        var entity = new UsuarioJpaEntity();
        entity.id = usuario.getId();
        entity.username = usuario.getUsername();
        entity.passwordHash = usuario.getPasswordHash();
        entity.papel = usuario.getPapel();
        entity.enabled = usuario.isEnabled();
        entity.clienteId = usuario.getClienteId();
        entity.createdAt = usuario.getCreatedAt();
        entity.updatedAt = usuario.getUpdatedAt();
        return entity;
    }

    private Usuario toDomain(UsuarioJpaEntity entity) {
        return new Usuario(entity.id, entity.username, entity.passwordHash, entity.papel, entity.enabled,
                entity.clienteId, entity.createdAt, entity.updatedAt);
    }
}

package br.com.fiap.techchallengefiap.presentation.rest.usuario;

import br.com.fiap.techchallengefiap.application.usuario.GerenciarUsuarioUseCase;
import br.com.fiap.techchallengefiap.domain.usuario.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
    private final GerenciarUsuarioUseCase useCase;

    public UsuarioController(GerenciarUsuarioUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        var usuario = useCase.criar(request.username(), request.password(), request.papel(), request.documentoCliente());
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + usuario.getId()))
                .body(resposta(usuario));
    }

    @GetMapping
    public PaginaUsuarioResponse listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var usuarios = useCase.listar(page, size);
        return new PaginaUsuarioResponse(usuarios.content().stream().map(this::resposta).toList(),
                usuarios.page(), usuarios.size(), usuarios.totalElements(), usuarios.totalPages());
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return resposta(useCase.buscar(id));
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarUsuarioRequest request) {
        return resposta(useCase.atualizar(id, request.papel(), request.documentoCliente(), request.enabled()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        useCase.desativar(id);
        return ResponseEntity.noContent().build();
    }

    private UsuarioResponse resposta(Usuario usuario) {
        return UsuarioResponse.from(usuario, useCase.documentoDoCliente(usuario));
    }
}

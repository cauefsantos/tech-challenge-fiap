package br.com.fiap.techchallengefiap.presentation.rest.cliente;

import br.com.fiap.techchallengefiap.application.cliente.GerenciarClienteUseCase;
import br.com.fiap.techchallengefiap.presentation.rest.shared.AtivoRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {
    private final GerenciarClienteUseCase useCase;

    public ClienteController(GerenciarClienteUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        var cliente = useCase.criar(request.tipoPessoa(), request.documento(), request.nome(), request.email(), request.telefone());
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + cliente.getId())).body(ClienteResponse.from(cliente));
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id) {
        return ClienteResponse.from(useCase.buscar(id));
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return useCase.listar().stream().map(ClienteResponse::from).toList();
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.from(useCase.atualizar(id, request.tipoPessoa(), request.documento(), request.nome(),
                request.email(), request.telefone()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        useCase.alterarAtivo(id, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativacao")
    public ClienteResponse alterarAtivo(@PathVariable Long id, @Valid @RequestBody AtivoRequest request) {
        return ClienteResponse.from(useCase.alterarAtivo(id, request.ativo()));
    }
}

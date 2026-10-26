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
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + cliente.getDocumento())).body(ClienteResponse.from(cliente));
    }

    @GetMapping("/{documento}")
    public ClienteResponse buscar(@PathVariable String documento) {
        return ClienteResponse.from(useCase.buscar(documento));
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return useCase.listar().stream().map(ClienteResponse::from).toList();
    }

    @PutMapping("/{documento}")
    public ClienteResponse atualizar(@PathVariable String documento, @Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.from(useCase.atualizar(documento, request.tipoPessoa(), request.documento(),
                request.nome(), request.email(), request.telefone()));
    }

    @DeleteMapping("/{documento}")
    public ResponseEntity<Void> desativar(@PathVariable String documento) {
        useCase.alterarAtivo(documento, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{documento}/ativacao")
    public ClienteResponse alterarAtivo(@PathVariable String documento, @Valid @RequestBody AtivoRequest request) {
        return ClienteResponse.from(useCase.alterarAtivo(documento, request.ativo()));
    }
}

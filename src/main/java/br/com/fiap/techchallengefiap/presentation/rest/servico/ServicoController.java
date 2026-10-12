package br.com.fiap.techchallengefiap.presentation.rest.servico;

import br.com.fiap.techchallengefiap.application.servico.GerenciarServicoUseCase;
import br.com.fiap.techchallengefiap.presentation.rest.shared.AtivoRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/servicos")
public class ServicoController {
    private final GerenciarServicoUseCase useCase;

    public ServicoController(GerenciarServicoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> criar(@Valid @RequestBody ServicoRequest request) {
        var servico = useCase.criar(request.codigo(), request.nome(), request.descricao(), request.valorBase(),
                request.tempoEstimadoSegundos());
        return ResponseEntity.created(URI.create("/api/v1/servicos/" + servico.getId())).body(ServicoResponse.from(servico));
    }

    @GetMapping("/{id}")
    public ServicoResponse buscar(@PathVariable Long id) {
        return ServicoResponse.from(useCase.buscar(id));
    }

    @GetMapping
    public List<ServicoResponse> listar() {
        return useCase.listar().stream().map(ServicoResponse::from).toList();
    }

    @PutMapping("/{id}")
    public ServicoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ServicoRequest request) {
        return ServicoResponse.from(useCase.atualizar(id, request.codigo(), request.nome(), request.descricao(),
                request.valorBase(), request.tempoEstimadoSegundos()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        useCase.alterarAtivo(id, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativacao")
    public ServicoResponse alterarAtivo(@PathVariable Long id, @Valid @RequestBody AtivoRequest request) {
        return ServicoResponse.from(useCase.alterarAtivo(id, request.ativo()));
    }
}

package br.com.fiap.techchallengefiap.presentation.rest.veiculo;

import br.com.fiap.techchallengefiap.application.veiculo.GerenciarVeiculoUseCase;
import br.com.fiap.techchallengefiap.presentation.rest.shared.AtivoRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/veiculos")
public class VeiculoController {
    private final GerenciarVeiculoUseCase useCase;

    public VeiculoController(GerenciarVeiculoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(@Valid @RequestBody VeiculoRequest request) {
        var veiculo = useCase.criar(request.clienteId(), request.placa(), request.marca(), request.modelo(), request.ano());
        return ResponseEntity.created(URI.create("/api/v1/veiculos/" + veiculo.getId())).body(VeiculoResponse.from(veiculo));
    }

    @GetMapping("/{id}")
    public VeiculoResponse buscar(@PathVariable Long id) {
        return VeiculoResponse.from(useCase.buscar(id));
    }

    @GetMapping
    public List<VeiculoResponse> listar() {
        return useCase.listar().stream().map(VeiculoResponse::from).toList();
    }

    @PutMapping("/{id}")
    public VeiculoResponse atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoRequest request) {
        return VeiculoResponse.from(useCase.atualizar(id, request.clienteId(), request.placa(), request.marca(),
                request.modelo(), request.ano()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        useCase.alterarAtivo(id, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativacao")
    public VeiculoResponse alterarAtivo(@PathVariable Long id, @Valid @RequestBody AtivoRequest request) {
        return VeiculoResponse.from(useCase.alterarAtivo(id, request.ativo()));
    }
}

package br.com.fiap.techchallengefiap.presentation.rest.veiculo;

import br.com.fiap.techchallengefiap.application.veiculo.GerenciarVeiculoUseCase;
import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;
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
        var veiculo = useCase.criar(request.documentoCliente(), request.placa(), request.marca(), request.modelo(), request.ano());
        return ResponseEntity.created(URI.create("/api/v1/veiculos/" + veiculo.getPlaca())).body(resposta(veiculo));
    }

    @GetMapping("/{placa}")
    public VeiculoResponse buscar(@PathVariable String placa) {
        return resposta(useCase.buscar(placa));
    }

    @GetMapping
    public List<VeiculoResponse> listar() {
        return useCase.listar().stream().map(this::resposta).toList();
    }

    @PutMapping("/{placa}")
    public VeiculoResponse atualizar(@PathVariable String placa, @Valid @RequestBody VeiculoRequest request) {
        return resposta(useCase.atualizar(placa, request.documentoCliente(), request.placa(), request.marca(),
                request.modelo(), request.ano()));
    }

    @DeleteMapping("/{placa}")
    public ResponseEntity<Void> desativar(@PathVariable String placa) {
        useCase.alterarAtivo(placa, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{placa}/ativacao")
    public VeiculoResponse alterarAtivo(@PathVariable String placa, @Valid @RequestBody AtivoRequest request) {
        return resposta(useCase.alterarAtivo(placa, request.ativo()));
    }

    private VeiculoResponse resposta(Veiculo veiculo) {
        return VeiculoResponse.from(veiculo, useCase.documentoDoCliente(veiculo));
    }
}

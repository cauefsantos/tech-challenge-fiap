package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import br.com.fiap.techchallengefiap.application.ordemservico.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/ordens-servico")
public class OrdemServicoController {
    private final CriarOrdemServicoUseCase criar;
    private final BuscarOrdemServicoUseCase buscar;

    public OrdemServicoController(CriarOrdemServicoUseCase criar, BuscarOrdemServicoUseCase buscar) {
        this.criar = criar;
        this.buscar = buscar;
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> criar(@Valid @RequestBody CriarOrdemServicoRequest request) {
        var os = criar.executar(new CriarOrdemServicoCommand(request.clienteId(), request.veiculoId()));
        return ResponseEntity.created(URI.create("/api/v1/ordens-servico/" + os.getId())).body(OrdemServicoResponse.from(os));
    }

    @GetMapping("/{id}")
    public OrdemServicoResponse buscar(@PathVariable Long id) {
        return OrdemServicoResponse.from(buscar.executar(id));
    }
}

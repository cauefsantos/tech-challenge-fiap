package br.com.fiap.techchallengefiap.presentation.rest.material;

import br.com.fiap.techchallengefiap.application.material.GerenciarMaterialUseCase;
import br.com.fiap.techchallengefiap.presentation.rest.shared.AtivoRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/materiais")
public class MaterialController {
    private final GerenciarMaterialUseCase useCase;

    public MaterialController(GerenciarMaterialUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<MaterialResponse> criar(@Valid @RequestBody MaterialRequest request) {
        var material = useCase.criar(request.codigo(), request.tipo(), request.nome(), request.descricao(),
                request.valorUnitario(), request.unidadeMedida(), request.quantidadeEstoque(), request.estoqueMinimo());
        return ResponseEntity.created(URI.create("/api/v1/materiais/" + material.getId())).body(MaterialResponse.from(material));
    }

    @GetMapping("/{id}")
    public MaterialResponse buscar(@PathVariable Long id) {
        return MaterialResponse.from(useCase.buscar(id));
    }

    @GetMapping
    public List<MaterialResponse> listar() {
        return useCase.listar().stream().map(MaterialResponse::from).toList();
    }

    @PutMapping("/{id}")
    public MaterialResponse atualizar(@PathVariable Long id, @Valid @RequestBody MaterialRequest request) {
        return MaterialResponse.from(useCase.atualizar(id, request.codigo(), request.tipo(), request.nome(),
                request.descricao(), request.valorUnitario(), request.unidadeMedida(),
                request.quantidadeEstoque(), request.estoqueMinimo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        useCase.alterarAtivo(id, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativacao")
    public MaterialResponse alterarAtivo(@PathVariable Long id, @Valid @RequestBody AtivoRequest request) {
        return MaterialResponse.from(useCase.alterarAtivo(id, request.ativo()));
    }
}

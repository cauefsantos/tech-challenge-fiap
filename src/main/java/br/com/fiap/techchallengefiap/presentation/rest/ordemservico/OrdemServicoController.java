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
    private final OrdemServicoFluxoUseCase fluxo;

    public OrdemServicoController(CriarOrdemServicoUseCase criar, BuscarOrdemServicoUseCase buscar,
                                 OrdemServicoFluxoUseCase fluxo) {
        this.criar = criar;
        this.buscar = buscar;
        this.fluxo = fluxo;
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

    @PostMapping("/{id}/diagnostico/inicio")
    public OrdemServicoResponse enviarParaDiagnostico(@PathVariable Long id) {
        return OrdemServicoResponse.from(fluxo.enviarParaDiagnostico(id));
    }

    @PostMapping("/{id}/diagnostico")
    public OrdemServicoResponse registrarDiagnostico(@PathVariable Long id, @Valid @RequestBody DiagnosticoRequest request) {
        return OrdemServicoResponse.from(fluxo.registrarDiagnostico(id, request.diagnostico()));
    }

    @PostMapping("/{id}/diagnostico/conclusao")
    public OrdemServicoResponse concluirDiagnostico(@PathVariable Long id) {
        return OrdemServicoResponse.from(fluxo.concluirDiagnostico(id));
    }

    @PostMapping("/{id}/itens-servico")
    public OrdemServicoResponse adicionarItemServico(@PathVariable Long id, @Valid @RequestBody ItemServicoRequest request) {
        return OrdemServicoResponse.from(fluxo.adicionarItemServico(id, request.servicoId(), request.quantidade()));
    }

    @DeleteMapping("/{id}/itens-servico/{itemId}")
    public OrdemServicoResponse removerItemServico(@PathVariable Long id, @PathVariable Long itemId) {
        return OrdemServicoResponse.from(fluxo.removerItemServico(id, itemId));
    }

    @PostMapping("/{id}/itens-material")
    public OrdemServicoResponse adicionarItemMaterial(@PathVariable Long id, @Valid @RequestBody ItemMaterialRequest request) {
        return OrdemServicoResponse.from(fluxo.adicionarItemMaterial(id, request.materialId(), request.quantidade()));
    }

    @DeleteMapping("/{id}/itens-material/{itemId}")
    public OrdemServicoResponse removerItemMaterial(@PathVariable Long id, @PathVariable Long itemId) {
        return OrdemServicoResponse.from(fluxo.removerItemMaterial(id, itemId));
    }

    @GetMapping
    public java.util.List<OrdemServicoResponse> listar() {
        return fluxo.listar().stream().map(OrdemServicoResponse::from).toList();
    }

    @PostMapping("/{id}/orcamentos")
    public OrcamentoResponse gerarOrcamento(@PathVariable Long id) {
        return OrcamentoResponse.from(fluxo.gerarOrcamento(id));
    }

    @PostMapping("/{id}/orcamentos/{orcamentoId}/disponibilizar")
    public OrdemServicoResponse disponibilizarOrcamento(@PathVariable Long id, @PathVariable Long orcamentoId) {
        return OrdemServicoResponse.from(fluxo.disponibilizarOrcamento(id, orcamentoId));
    }

    @PostMapping("/{id}/orcamentos/{orcamentoId}/aprovar")
    public OrdemServicoResponse aprovarOrcamento(@PathVariable Long id, @PathVariable Long orcamentoId,
                                                @RequestBody(required = false) AprovacaoOrcamentoRequest request) {
        var texto = request == null ? null : request.observacao();
        return OrdemServicoResponse.from(fluxo.aprovarOrcamento(id, orcamentoId, texto));
    }

    @PostMapping("/{id}/orcamentos/{orcamentoId}/rejeitar")
    public OrdemServicoResponse rejeitarOrcamento(@PathVariable Long id, @PathVariable Long orcamentoId,
                                                @RequestBody(required = false) AprovacaoOrcamentoRequest request) {
        var texto = request == null ? null : request.observacao();
        return OrdemServicoResponse.from(fluxo.rejeitarOrcamento(id, orcamentoId, texto));
    }

    @PostMapping("/{id}/finalizacao")
    public OrdemServicoResponse finalizar(@PathVariable Long id) {
        return OrdemServicoResponse.from(fluxo.finalizar(id));
    }

    @PostMapping("/{id}/entrega")
    public OrdemServicoResponse entregar(@PathVariable Long id) {
        return OrdemServicoResponse.from(fluxo.entregar(id));
    }
}

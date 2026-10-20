package br.com.fiap.techchallengefiap.presentation.rest.ordemservico;

import br.com.fiap.techchallengefiap.application.ordemservico.ConsultarOrdemServicoPublicaUseCase;
import br.com.fiap.techchallengefiap.application.ordemservico.MetricasOrdemServicoUseCase;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class PublicOrdemServicoController {
    private final ConsultarOrdemServicoPublicaUseCase consultaPublica;
    private final MetricasOrdemServicoUseCase metricas;

    public PublicOrdemServicoController(ConsultarOrdemServicoPublicaUseCase consultaPublica, MetricasOrdemServicoUseCase metricas) {
        this.consultaPublica = consultaPublica;
        this.metricas = metricas;
    }

    @GetMapping("/public/ordens-servico/{numero}")
    public ResponseEntity<PublicOrdemServicoResponse> consultarPorNumero(
            @PathVariable String numero,
            @RequestParam(required = false) String documento) {
        try {
            return ResponseEntity.ok(consultaPublica.executar(numero, documento));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ordens-servico/metricas/tempo-medio-execucao")
    public MetricasOrdemServicoUseCase.TempoMedioExecucaoResponse tempoMedioExecucao() {
        return metricas.executar();
    }
}

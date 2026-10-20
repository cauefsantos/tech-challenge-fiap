package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
public class MetricasOrdemServicoUseCase {
    private final OrdemServicoRepository repository;

    public MetricasOrdemServicoUseCase(OrdemServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public TempoMedioExecucaoResponse executar() {
        List<OrdemServico> ordens = repository.listar().stream()
                .filter(os -> os.getStatus() == StatusOrdemServico.FINALIZADA || os.getStatus() == StatusOrdemServico.ENTREGUE)
                .filter(os -> os.getDataInicioExecucao() != null && os.getDataFinalizacao() != null)
                .toList();

        if (ordens.isEmpty()) {
            return new TempoMedioExecucaoResponse(0L, null);
        }

        Duration total = ordens.stream()
                .map(os -> Duration.between(os.getDataInicioExecucao(), os.getDataFinalizacao()))
                .reduce(Duration.ZERO, Duration::plus);

        long segundosMedios = total.dividedBy(ordens.size()).getSeconds();
        return new TempoMedioExecucaoResponse((long) ordens.size(), segundosMedios);
    }

    public record TempoMedioExecucaoResponse(Long amostras, Long tempoMedioSegundos) {
    }
}

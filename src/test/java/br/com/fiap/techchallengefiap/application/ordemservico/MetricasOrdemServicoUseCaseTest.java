package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricasOrdemServicoUseCaseTest {
    @Mock OrdemServicoRepository repository;
    @InjectMocks MetricasOrdemServicoUseCase useCase;

    @Test
    void retornaZeroAmostrasETempoNuloSemOrdensElegiveis() {
        when(repository.listar()).thenReturn(List.of(OrdemServicoUseCaseFixture.os(StatusOrdemServico.RECEBIDA)));
        var resultado = useCase.executar();
        assertEquals(0L, resultado.amostras());
        assertNull(resultado.tempoMedioSegundos());
    }

    @Test
    void calculaTempoParaUmaOrdemFinalizada() {
        when(repository.listar()).thenReturn(List.of(OrdemServicoUseCaseFixture.osFinalizada()));
        var resultado = useCase.executar();
        assertEquals(1L, resultado.amostras());
        assertEquals(120L, resultado.tempoMedioSegundos());
    }

    @Test
    void calculaMediaInteiraParaVariasOrdensElegiveis() {
        var primeira = OrdemServicoUseCaseFixture.osFinalizada();
        var segunda = new OrdemServico(11L, "OS-11", 20L, 30L, StatusOrdemServico.ENTREGUE,
                true, BigDecimal.TEN, OrdemServicoUseCaseFixture.DATA, null, null,
                OrdemServicoUseCaseFixture.DATA, OrdemServicoUseCaseFixture.DATA.plusSeconds(181), null, null);
        when(repository.listar()).thenReturn(List.of(primeira, segunda));
        var resultado = useCase.executar();
        assertEquals(2L, resultado.amostras());
        assertEquals(150L, resultado.tempoMedioSegundos());
    }

    @Test
    void ignoraOrdensNaoFinalizadasOuSemDatasObrigatorias() {
        var pendente = OrdemServicoUseCaseFixture.os(StatusOrdemServico.EM_EXECUCAO);
        var semInicio = new OrdemServico(12L, "OS-12", 20L, 30L, StatusOrdemServico.FINALIZADA,
                true, BigDecimal.TEN, OrdemServicoUseCaseFixture.DATA, null, null, null,
                OrdemServicoUseCaseFixture.DATA.plusSeconds(60), null, null);
        var semFim = new OrdemServico(13L, "OS-13", 20L, 30L, StatusOrdemServico.ENTREGUE,
                true, BigDecimal.TEN, OrdemServicoUseCaseFixture.DATA, null, null,
                OrdemServicoUseCaseFixture.DATA, null, null, null);
        when(repository.listar()).thenReturn(List.of(pendente, semInicio, semFim));
        var resultado = useCase.executar();
        assertEquals(0L, resultado.amostras());
        assertNull(resultado.tempoMedioSegundos());
    }
}

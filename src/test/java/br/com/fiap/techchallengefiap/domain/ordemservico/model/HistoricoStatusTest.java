package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HistoricoStatusTest {
    @Test
    void criaHistoricoComStatusNovoEObservacaoNormalizada() {
        HistoricoStatus historico = new HistoricoStatus(1L, StatusOrdemServico.RECEBIDA,
                StatusOrdemServico.EM_DIAGNOSTICO, "  Início do diagnóstico  ");

        assertNull(historico.getId());
        assertEquals(1L, historico.getOrdemServicoId());
        assertEquals(StatusOrdemServico.RECEBIDA, historico.getStatusAnterior());
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, historico.getStatusNovo());
        assertEquals("Início do diagnóstico", historico.getObservacao());
        assertNotNull(historico.getDataAlteracao());
    }

    @Test
    void rejeitaStatusNovoOuDataAusentes() {
        assertThrows(DomainException.class,
                () -> new HistoricoStatus(1L, StatusOrdemServico.RECEBIDA, null, null));
        assertThrows(NullPointerException.class,
                () -> new HistoricoStatus(null, 1L, StatusOrdemServico.RECEBIDA,
                        StatusOrdemServico.EM_DIAGNOSTICO, null, null));
    }

    @Test
    void permiteStatusAnteriorNuloNaPrimeiraTransicao() {
        HistoricoStatus historico = new HistoricoStatus(null, 7L, null,
                StatusOrdemServico.RECEBIDA, OrdemServicoFixture.DATA, null);

        assertNull(historico.getStatusAnterior());
        assertNull(historico.getObservacao());
        assertEquals(OrdemServicoFixture.DATA, historico.getDataAlteracao());
    }

    @Test
    void registraQueSetterDeIdNaoAlteraOIdReconstruido() {
        HistoricoStatus historico = new HistoricoStatus(3L, 1L, StatusOrdemServico.RECEBIDA,
                StatusOrdemServico.EM_DIAGNOSTICO, OrdemServicoFixture.DATA, null);
        historico.setId(9L);

        assertEquals(3L, historico.getId());
    }
}

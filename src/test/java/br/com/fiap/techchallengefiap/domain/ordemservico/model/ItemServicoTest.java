package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.servico.model.Servico;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ItemServicoTest {
    @Test
    void criaSnapshotDePrecoDescricaoETempoDoServico() {
        Servico servico = OrdemServicoFixture.servico();
        ItemServico item = new ItemServico(10L, servico, new BigDecimal("1.5"));
        servico.desativar();

        assertEquals(3L, item.getServicoId());
        assertEquals("Troca de oleo", item.getDescricao());
        assertEquals(new BigDecimal("100.00"), item.getValorUnitario());
        assertEquals(new BigDecimal("150.00"), item.getSubtotal());
        assertEquals(3600L, item.getTempoEstimadoSegundos());
    }

    @Test
    void rejeitaServicoInexistenteOuInativo() {
        assertThrows(DomainException.class, () -> new ItemServico(1L, null, BigDecimal.ONE));
        Servico semId = new Servico("S1", "Servico", null, BigDecimal.ONE, Duration.ofMinutes(1));
        assertThrows(DomainException.class, () -> new ItemServico(1L, semId, BigDecimal.ONE));
        Servico inativo = OrdemServicoFixture.servico();
        inativo.desativar();
        assertThrows(DomainException.class, () -> new ItemServico(1L, inativo, BigDecimal.ONE));
    }

    @Test
    void rejeitaQuantidadeNaoPositiva() {
        assertThrows(DomainException.class, () -> new ItemServico(1L, OrdemServicoFixture.servico(), null));
        assertThrows(DomainException.class, () -> new ItemServico(1L, OrdemServicoFixture.servico(), BigDecimal.ZERO));
        assertThrows(DomainException.class, () -> new ItemServico(1L, OrdemServicoFixture.servico(), BigDecimal.ONE.negate()));
    }

    @Test
    void rejeitaReconstrucaoComServicoPrecoTempoOuSubtotalInvalidos() {
        assertThrows(DomainException.class, () -> new ItemServico(1L, 1L, null, "Servico", BigDecimal.ONE, BigDecimal.ONE, null, 1));
        assertThrows(DomainException.class, () -> new ItemServico(1L, 1L, 1L, "Servico", BigDecimal.ONE, new BigDecimal("-1"), null, 1));
        assertThrows(DomainException.class, () -> new ItemServico(1L, 1L, 1L, "Servico", BigDecimal.ONE, BigDecimal.ONE, null, 0));
        assertThrows(DomainException.class, () -> new ItemServico(1L, 1L, 1L, "Servico", BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("2.00"), 1));
    }
}

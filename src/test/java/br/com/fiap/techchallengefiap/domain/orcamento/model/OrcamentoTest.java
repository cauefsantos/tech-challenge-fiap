package br.com.fiap.techchallengefiap.domain.orcamento.model;

import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServicoFixture;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrcamentoTest {
    private ItemServico servico() {
        return new ItemServico(1L, 10L, 3L, "Servico", new BigDecimal("2"),
                new BigDecimal("10.00"), new BigDecimal("20.00"), 60);
    }

    private ItemMaterial material(TipoMaterial tipo) {
        return new ItemMaterial(1L, 10L, 4L, tipo, "Material", new BigDecimal("2"),
                new BigDecimal("5.00"), new BigDecimal("10.00"));
    }

    private Orcamento reconstruido(StatusOrcamento status) {
        return new Orcamento(1L, 10L, 1, new BigDecimal("20.00"), new BigDecimal("10.00"),
                new BigDecimal("5.00"), new BigDecimal("35.00"), status,
                OrdemServicoFixture.DATA, null, null);
    }

    @Test
    void calculaValoresDeServicosPecasInsumosETotal() {
        Orcamento orcamento = new Orcamento(10L, 1, List.of(servico()),
                List.of(material(TipoMaterial.PECA), material(TipoMaterial.INSUMO)));

        assertEquals(new BigDecimal("20.00"), orcamento.getValorServicos());
        assertEquals(new BigDecimal("10.00"), orcamento.getValorPecas());
        assertEquals(new BigDecimal("10.00"), orcamento.getValorInsumos());
        assertEquals(new BigDecimal("40.00"), orcamento.getValorTotal());
    }

    @Test
    void rejeitaOrdemServicoOuVersaoNaoPositiva() {
        assertThrows(DomainException.class, () -> new Orcamento(null, 1, List.of(), List.of()));
        assertThrows(DomainException.class, () -> new Orcamento(0L, 1, List.of(), List.of()));
        assertThrows(DomainException.class, () -> new Orcamento(1L, 0, List.of(), List.of()));
    }

    @Test
    void geraOrcamentoPendenteComDataDeGeracao() {
        Orcamento orcamento = new Orcamento(10L, 1, List.of(), List.of());

        assertEquals(StatusOrcamento.PENDENTE, orcamento.getStatus());
        assertEquals(10L, orcamento.getOrdemServicoId());
        assertEquals(1, orcamento.getVersao());
        assertNotNull(orcamento.getDataGeracao());
        assertNull(orcamento.getDataResposta());
    }

    @Test
    void disponibilizaSomenteOrcamentoPendente() {
        Orcamento orcamento = reconstruido(StatusOrcamento.PENDENTE);
        orcamento.disponibilizar();
        assertEquals(StatusOrcamento.DISPONIBILIZADO, orcamento.getStatus());
        assertThrows(DomainException.class, orcamento::disponibilizar);
    }

    @Test
    void aprovaOuRejeitaSomenteOrcamentoDisponibilizado() {
        Orcamento aprovado = reconstruido(StatusOrcamento.PENDENTE);
        assertThrows(DomainException.class, () -> aprovado.aprovar("sim"));
        aprovado.disponibilizar();
        OffsetDateTime antesDaResposta = OffsetDateTime.now().minusSeconds(1);
        aprovado.aprovar("  aprovado  ");
        assertEquals(StatusOrcamento.APROVADO, aprovado.getStatus());
        assertEquals("aprovado", aprovado.getObservacaoResposta());
        assertNotNull(aprovado.getDataResposta());
        assertTrue(aprovado.getDataResposta().isAfter(antesDaResposta));

        Orcamento rejeitado = reconstruido(StatusOrcamento.PENDENTE);
        rejeitado.disponibilizar();
        rejeitado.rejeitar("não");
        assertEquals(StatusOrcamento.REJEITADO, rejeitado.getStatus());
    }

    @Test
    void normalizaObservacaoDaResposta() {
        Orcamento orcamento = reconstruido(StatusOrcamento.PENDENTE);
        orcamento.disponibilizar();
        orcamento.aprovar("  ");

        assertNull(orcamento.getObservacaoResposta());
    }

    @Test
    void cancelaSomenteOrcamentoAindaNaoAprovadoOuCancelado() {
        Orcamento pendente = reconstruido(StatusOrcamento.PENDENTE);
        pendente.cancelar();
        assertEquals(StatusOrcamento.CANCELADO, pendente.getStatus());

        Orcamento disponibilizado = reconstruido(StatusOrcamento.DISPONIBILIZADO);
        disponibilizado.cancelar();
        assertEquals(StatusOrcamento.CANCELADO, disponibilizado.getStatus());

        Orcamento rejeitado = reconstruido(StatusOrcamento.REJEITADO);
        rejeitado.cancelar();
        assertEquals(StatusOrcamento.CANCELADO, rejeitado.getStatus());
        assertThrows(DomainException.class, () -> reconstruido(StatusOrcamento.APROVADO).cancelar());
        assertThrows(DomainException.class, () -> reconstruido(StatusOrcamento.CANCELADO).cancelar());
    }

    @Test
    void atualizaValoresSomenteEmEstadosEditaveis() {
        Orcamento orcamento = reconstruido(StatusOrcamento.PENDENTE);
        orcamento.atualizarValores(List.of(servico()), List.of(material(TipoMaterial.PECA)));
        assertEquals(new BigDecimal("30.00"), orcamento.getValorTotal());
        orcamento.disponibilizar();
        orcamento.atualizarValores(List.of(), List.of(material(TipoMaterial.INSUMO)));
        assertEquals(new BigDecimal("10.00"), orcamento.getValorInsumos());
        orcamento.aprovar(null);
        assertThrows(DomainException.class,
                () -> orcamento.atualizarValores(List.of(), List.of()));
    }

    @Test
    void rejeitaListasNulasOuTotalReconstruidoDivergente() {
        Orcamento orcamento = new Orcamento(10L, 1, List.of(), List.of());
        assertThrows(DomainException.class, () -> orcamento.atualizarValores(null, List.of()));
        assertThrows(DomainException.class, () -> orcamento.atualizarValores(List.of(), null));
        assertThrows(DomainException.class,
                () -> new Orcamento(1L, 10L, 1, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        new BigDecimal("2"), StatusOrcamento.PENDENTE, OrdemServicoFixture.DATA, null, null));
        assertThrows(DomainException.class,
                () -> new Orcamento(1L, 10L, 1, new BigDecimal("-1"), BigDecimal.ZERO, BigDecimal.ZERO,
                        null, StatusOrcamento.PENDENTE, OrdemServicoFixture.DATA, null, null));
        assertThrows(NullPointerException.class,
                () -> new Orcamento(1L, 10L, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        null, null, OrdemServicoFixture.DATA, null, null));
        assertThrows(NullPointerException.class,
                () -> new Orcamento(1L, 10L, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        null, StatusOrcamento.PENDENTE, null, null, null));
        assertNotNull(orcamento);
    }
}

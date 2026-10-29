package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.orcamento.model.Orcamento;
import br.com.fiap.techchallengefiap.domain.orcamento.model.StatusOrcamento;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {
    @Test
    void iniciaRecebidaComNumeroEValorZeroERejeitaClienteOuVeiculoInvalidos() {
        OrdemServico ordem = new OrdemServico(1L, 2L);

        assertEquals(StatusOrdemServico.RECEBIDA, ordem.getStatus());
        assertEquals(BigDecimal.ZERO, ordem.getValorTotal());
        assertNotNull(ordem.getNumero());
        assertEquals(8, ordem.getNumero().length());
        assertNotNull(ordem.getCriadaEm());
        assertThrows(DomainException.class, () -> new OrdemServico(null, 2L));
        assertThrows(DomainException.class, () -> new OrdemServico(1L, 0L));
    }

    @Test
    void transicionaRecebidaParaDiagnostico() {
        OrdemServico ordem = OrdemServicoFixture.novaOrdemPersistida();
        ordem.enviarParaDiagnostico();

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordem.getStatus());
        assertHistorico(ordem, 0, StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO);
    }

    @Test
    void transicionaDiagnosticoParaAguardandoAprovacao() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        ordem.aguardarAprovacao(new BigDecimal("150.00"));

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, ordem.getStatus());
        assertEquals(new BigDecimal("150.00"), ordem.getValorTotal());
        assertHistorico(ordem, 1, StatusOrdemServico.EM_DIAGNOSTICO, StatusOrdemServico.AGUARDANDO_APROVACAO);
    }

    @Test
    void transicionaAguardandoAprovacaoParaExecucao() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        Orcamento orcamento = ordem.gerarOrcamento();
        ordem.disponibilizarOrcamento(orcamento);
        ordem.aprovarOrcamento(orcamento, "Aprovado");

        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus());
        assertTrue(ordem.isOrcamentoAprovado());
        assertNotNull(ordem.getDataInicioExecucao());
        assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
        assertHistorico(ordem, 2, StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.EM_EXECUCAO);
    }

    @Test
    void transicionaExecucaoParaFinalizada() {
        OrdemServico ordem = ordemEmExecucao();
        ordem.finalizar();

        assertEquals(StatusOrdemServico.FINALIZADA, ordem.getStatus());
        assertNotNull(ordem.getDataFinalizacao());
        assertHistorico(ordem, 3, StatusOrdemServico.EM_EXECUCAO, StatusOrdemServico.FINALIZADA);
    }

    @Test
    void transicionaFinalizadaParaEntregue() {
        OrdemServico ordem = ordemEmExecucao();
        ordem.finalizar();
        ordem.entregar();

        assertEquals(StatusOrdemServico.ENTREGUE, ordem.getStatus());
        assertNotNull(ordem.getDataEntrega());
        assertHistorico(ordem, 4, StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE);
    }

    @Test
    void transicionaAguardandoAprovacaoParaCanceladaQuandoRejeita() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        Orcamento orcamento = ordem.gerarOrcamento();
        ordem.disponibilizarOrcamento(orcamento);
        ordem.rejeitarOrcamento(orcamento, "  Recusado  ");

        assertEquals(StatusOrdemServico.CANCELADA, ordem.getStatus());
        assertEquals(StatusOrcamento.REJEITADO, orcamento.getStatus());
        assertEquals("Recusado", orcamento.getObservacaoResposta());
        assertHistorico(ordem, 2, StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.CANCELADA);
    }

    @Test
    void rejeitaCadaTransicaoEmEstadoInvalido() {
        OrdemServico ordem = OrdemServicoFixture.novaOrdemPersistida();
        assertThrows(DomainException.class, ordem::entregar);
        assertThrows(DomainException.class, ordem::finalizar);
        assertThrows(DomainException.class, ordem::aprovarOrcamento);
        assertThrows(DomainException.class, () -> ordem.aguardarAprovacao(BigDecimal.ONE));
        assertThrows(DomainException.class, () -> ordem.registrarDiagnostico("diagnóstico"));
        assertThrows(DomainException.class, ordem::concluirDiagnostico);
        ordem.enviarParaDiagnostico();
        assertThrows(DomainException.class, ordem::enviarParaDiagnostico);
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordem.getStatus());
        assertEquals(1, ordem.getHistoricoStatus().size());
    }

    @Test
    void registraHistoricoEmCadaTransicaoComStatusAnteriorENovo() {
        OrdemServico ordem = ordemEmExecucao();
        ordem.finalizar();
        ordem.entregar();

        assertEquals(5, ordem.getHistoricoStatus().size());
        for (int indice = 0; indice < ordem.getHistoricoStatus().size(); indice++) {
            assertNotNull(ordem.getHistoricoStatus().get(indice).getDataAlteracao());
        }
        assertEquals(StatusOrdemServico.RECEBIDA, ordem.getHistoricoStatus().get(0).getStatusAnterior());
        assertEquals(StatusOrdemServico.ENTREGUE, ordem.getHistoricoStatus().get(4).getStatusNovo());
    }

    @Test
    void registraEConcluiDiagnosticoSomenteEmDiagnostico() {
        OrdemServico ordem = OrdemServicoFixture.novaOrdemPersistida();
        assertThrows(DomainException.class, () -> ordem.registrarDiagnostico("feito"));
        ordem.enviarParaDiagnostico();
        assertThrows(DomainException.class, () -> ordem.registrarDiagnostico(" "));
        ordem.registrarDiagnostico("  Trocar filtro  ");
        assertEquals("Trocar filtro", ordem.getDiagnostico());
        ordem.concluirDiagnostico();

        // A implementação atual valida o diagnóstico, mas não muda status nem histórico.
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordem.getStatus());
        assertEquals(1, ordem.getHistoricoStatus().size());
        assertThrows(DomainException.class, () -> OrdemServicoFixture.novaOrdemPersistida().concluirDiagnostico());
    }

    @Test
    void adicionaItensSomenteEmDiagnosticoERecalculaValorTotal() {
        OrdemServico ordem = OrdemServicoFixture.novaOrdemPersistida();
        assertThrows(DomainException.class, () -> ordem.adicionarItemServico(OrdemServicoFixture.itemServico()));
        ordem.enviarParaDiagnostico();
        ordem.adicionarItemServico(OrdemServicoFixture.itemServico());
        ordem.adicionarItemMaterial(OrdemServicoFixture.itemMaterial(TipoMaterial.PECA));

        assertEquals(1, ordem.getItensServico().size());
        assertEquals(1, ordem.getItensMaterial().size());
        assertEquals(new BigDecimal("150.00"), ordem.getValorTotal());
    }

    @Test
    void rejeitaItemNuloOuDeOutraOrdem() {
        OrdemServico ordem = OrdemServicoFixture.novaOrdemPersistida();
        ordem.enviarParaDiagnostico();
        assertThrows(DomainException.class, () -> ordem.adicionarItemServico(null));
        assertThrows(DomainException.class, () -> ordem.adicionarItemMaterial(null));
        assertThrows(DomainException.class,
                () -> ordem.adicionarItemServico(new ItemServico(99L, OrdemServicoFixture.servico(), BigDecimal.ONE)));
        assertThrows(DomainException.class,
                () -> ordem.adicionarItemMaterial(new ItemMaterial(99L, OrdemServicoFixture.material(TipoMaterial.PECA), BigDecimal.ONE)));
        assertTrue(ordem.getItensServico().isEmpty());
        assertTrue(ordem.getItensMaterial().isEmpty());
    }

    @Test
    void removeItensERecalculaValorTotal() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        ItemServico servico = new ItemServico(7L, 10L, 3L, "Servico", BigDecimal.ONE,
                new BigDecimal("100.00"), new BigDecimal("100.00"), 60);
        ItemMaterial material = new ItemMaterial(8L, 10L, 4L, TipoMaterial.PECA, "Material",
                BigDecimal.ONE, new BigDecimal("25.00"), new BigDecimal("25.00"));
        ordem.carregarItensPersistidos(List.of(servico));
        ordem.carregarMateriaisPersistidos(List.of(material));
        ordem.removerItemServico(7L);
        assertEquals(new BigDecimal("25.00"), ordem.getValorTotal());
        ordem.removerItemMaterial(8L);
        assertEquals(BigDecimal.ZERO, ordem.getValorTotal());
    }

    @Test
    void rejeitaRemocaoDeItemInexistente() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        assertThrows(DomainException.class, () -> ordem.removerItemServico(99L));
        assertThrows(DomainException.class, () -> ordem.removerItemMaterial(99L));
    }

    @Test
    void geraOrcamentosComVersoesCrescentes() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        assertEquals(1, ordem.gerarOrcamento().getVersao());
        assertEquals(2, ordem.gerarOrcamento().getVersao());
    }

    @Test
    void rejeitaGeracaoSemIdPersistidoOuForaDeDiagnostico() {
        OrdemServico semId = new OrdemServico(null, "OS-SEM-ID", 1L, 2L,
                StatusOrdemServico.RECEBIDA, false, BigDecimal.ZERO,
                OrdemServicoFixture.DATA, null, null, null, null, null, null);
        semId.enviarParaDiagnostico();
        assertThrows(DomainException.class, semId::gerarOrcamento);
        assertThrows(DomainException.class, () -> OrdemServicoFixture.novaOrdemPersistida().gerarOrcamento());
    }

    @Test
    void rejeitaDisponibilizacaoDeOrcamentoDeOutraOs() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        Orcamento externo = new Orcamento(10L, 1, List.of(), List.of());
        assertThrows(DomainException.class, () -> ordem.disponibilizarOrcamento(externo));
    }

    @Test
    void rejeitaValorNuloNegativoOuDivergenteDaSomaDosItens() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        assertThrows(DomainException.class, () -> ordem.aguardarAprovacao(null));
        assertThrows(DomainException.class, () -> ordem.aguardarAprovacao(BigDecimal.ZERO));
        assertThrows(DomainException.class, () -> ordem.aguardarAprovacao(new BigDecimal("-1")));
        assertThrows(DomainException.class, () -> ordem.aguardarAprovacao(new BigDecimal("149")));
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordem.getStatus());
        assertEquals(new BigDecimal("150.00"), ordem.getValorTotal());
    }

    @Test
    void disponibilizacaoComTotalDivergenteRevelaAtualizacaoParcialDoOrcamento() {
        OrdemServico ordem = OrdemServicoFixture.novaOrdemPersistida();
        ordem.enviarParaDiagnostico();
        ordem.adicionarItemServico(OrdemServicoFixture.itemServico());
        Orcamento divergente = new Orcamento(2L, 10L, 1, new BigDecimal("101.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("101.00"),
                StatusOrcamento.PENDENTE, OrdemServicoFixture.DATA, null, null);
        ordem.carregarOrcamentosPersistidos(List.of(divergente));

        assertThrows(DomainException.class, () -> ordem.disponibilizarOrcamento(divergente));
        assertEquals(StatusOrcamento.DISPONIBILIZADO, divergente.getStatus());
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordem.getStatus());
        assertEquals(new BigDecimal("100.00"), ordem.getValorTotal());
    }

    @Test
    void aprovaOrcamentoUmaUnicaVezEIniciaExecucao() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        Orcamento orcamento = ordem.gerarOrcamento();
        orcamento.setId(5L);
        ordem.disponibilizarOrcamento(orcamento);
        ordem.aprovarOrcamento(orcamento.getId(), "sim");
        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus());
        assertThrows(DomainException.class, () -> ordem.aprovarOrcamento(orcamento, "novamente"));
        assertThrows(DomainException.class, () -> ordem.aprovarOrcamento(999L, "não existe"));
        assertThrows(DomainException.class, () -> ordem.aprovarOrcamento((Orcamento) null, null));
    }

    @Test
    void rejeitaRejeicaoForaDeAguardandoAprovacaoOuParaOrcamentoExterno() {
        OrdemServico ordem = OrdemServicoFixture.novaOrdemPersistida();
        Orcamento externo = new Orcamento(10L, 1, List.of(), List.of());
        assertThrows(DomainException.class, () -> ordem.rejeitarOrcamento(externo, "não"));
        assertThrows(DomainException.class, () -> ordem.rejeitarOrcamento(99L, "não"));
    }

    @Test
    void impedeFinalizacaoSemAprovacaoOuSemItemServico() {
        OrdemServico semAprovacao = OrdemServicoFixture.novaOrdemPersistida();
        assertThrows(DomainException.class, semAprovacao::finalizar);

        OrdemServico semServico = new OrdemServico(10L, "OS-10", 1L, 2L,
                StatusOrdemServico.EM_EXECUCAO, true, BigDecimal.ONE,
                OrdemServicoFixture.DATA, null, null, null, null, null, null);
        assertThrows(DomainException.class, semServico::finalizar);
    }

    @Test
    void impedeEdicaoAposAprovacaoEntregaOuCancelamento() {
        OrdemServico aprovada = ordemEmExecucao();
        assertThrows(DomainException.class, () -> aprovada.adicionarItemServico(OrdemServicoFixture.itemServico()));
        assertThrows(DomainException.class, () -> aprovada.definirObservacoes("alterada"));

        OrdemServico entregue = ordemEmExecucao();
        entregue.finalizar();
        entregue.entregar();
        assertThrows(DomainException.class, () -> entregue.definirObservacoes("tarde"));

        OrdemServico cancelada = OrdemServicoFixture.emDiagnosticoComItens();
        Orcamento orcamento = cancelada.gerarOrcamento();
        cancelada.disponibilizarOrcamento(orcamento);
        cancelada.rejeitarOrcamento(orcamento, null);
        assertThrows(DomainException.class, () -> cancelada.definirObservacoes("tarde"));
    }

    @Test
    void atualizaOrcamentoPendenteQuandoItensMudam() {
        OrdemServico ordem = OrdemServicoFixture.novaOrdemPersistida();
        ordem.enviarParaDiagnostico();
        ordem.adicionarItemServico(OrdemServicoFixture.itemServico());
        Orcamento orcamento = ordem.gerarOrcamento();
        ordem.adicionarItemServico(OrdemServicoFixture.itemServico());

        assertEquals(new BigDecimal("200.00"), orcamento.getValorTotal());
        ordem.disponibilizarOrcamento(orcamento);
        ordem.aprovarOrcamento(orcamento, null);
        assertThrows(DomainException.class, () -> ordem.adicionarItemServico(OrdemServicoFixture.itemServico()));
    }

    @Test
    void carregaItensMateriaisOrcamentosEHistoricoPersistidosSubstituindoConteudoAnterior() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        ItemServico item = new ItemServico(7L, 10L, 3L, "Novo", BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 1);
        ItemMaterial material = new ItemMaterial(8L, 10L, 4L, TipoMaterial.PECA, "Novo",
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        Orcamento orcamento = new Orcamento(10L, 1, List.of(), List.of());
        HistoricoStatus historico = new HistoricoStatus(null, 10L, null, StatusOrdemServico.RECEBIDA,
                OrdemServicoFixture.DATA, null);
        ordem.carregarItensPersistidos(List.of(item));
        ordem.carregarMateriaisPersistidos(List.of(material));
        ordem.carregarOrcamentosPersistidos(List.of(orcamento));
        ordem.carregarHistoricoPersistido(List.of(historico));

        assertEquals(List.of(item), ordem.getItensServico());
        assertEquals(List.of(material), ordem.getItensMaterial());
        assertEquals(List.of(orcamento), ordem.getOrcamentos());
        assertEquals(List.of(historico), ordem.getHistoricoStatus());
        assertThrows(UnsupportedOperationException.class, () -> ordem.getItensServico().clear());
    }

    @Test
    void carregaListasNulasComoVazias() {
        OrdemServico ordem = OrdemServicoFixture.novaOrdemPersistida();
        ordem.carregarItensPersistidos(null);
        ordem.carregarMateriaisPersistidos(null);
        ordem.carregarOrcamentosPersistidos(null);
        ordem.carregarHistoricoPersistido(null);

        assertTrue(ordem.getItensServico().isEmpty());
        assertTrue(ordem.getItensMaterial().isEmpty());
        assertTrue(ordem.getOrcamentos().isEmpty());
        assertTrue(ordem.getHistoricoStatus().isEmpty());
    }

    @Test
    void normalizaObservacoesERegistraDescricaoDoProblema() {
        OrdemServico ordem = OrdemServicoFixture.novaOrdemPersistida();
        assertThrows(DomainException.class, () -> ordem.definirDescricaoProblema(" "));
        ordem.definirDescricaoProblema("  Barulho  ");
        ordem.definirObservacoes("  Retornar ligação  ");
        assertEquals("Barulho", ordem.getDescricaoProblema());
        assertEquals("Retornar ligação", ordem.getObservacoes());
        ordem.definirObservacoes(" ");
        assertNull(ordem.getObservacoes());
    }

    private static OrdemServico ordemEmExecucao() {
        OrdemServico ordem = OrdemServicoFixture.emDiagnosticoComItens();
        Orcamento orcamento = ordem.gerarOrcamento();
        ordem.disponibilizarOrcamento(orcamento);
        ordem.aprovarOrcamento(orcamento, "aprovado");
        return ordem;
    }

    private static void assertHistorico(OrdemServico ordem, int indice,
                                        StatusOrdemServico anterior, StatusOrdemServico novo) {
        HistoricoStatus historico = ordem.getHistoricoStatus().get(indice);
        assertEquals(anterior, historico.getStatusAnterior());
        assertEquals(novo, historico.getStatusNovo());
        assertNotNull(historico.getDataAlteracao());
    }
}

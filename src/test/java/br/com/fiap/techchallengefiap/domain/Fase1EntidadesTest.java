package br.com.fiap.techchallengefiap.domain;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.orcamento.model.Orcamento;
import br.com.fiap.techchallengefiap.domain.orcamento.model.StatusOrcamento;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.HistoricoStatus;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
import br.com.fiap.techchallengefiap.domain.servico.model.Servico;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Fase1EntidadesTest {
    @Test
    void validaDocumentoDoClienteEPermiteDesativacaoHistorica() {
        Cliente cliente = new Cliente(TipoPessoa.FISICA, "529.982.247-25", "Cliente Teste", null, null);
        assertEquals("52998224725", cliente.getDocumento());
        cliente.desativar();
        assertFalse(cliente.isAtivo());
        assertThrows(DomainException.class,
                () -> new Cliente(TipoPessoa.FISICA, "11111111111", "Invalido", null, null));
    }

    @Test
    void normalizaEValidaPlacaEVinculoDoVeiculo() {
        Veiculo veiculo = new Veiculo(1L, "abc-1d23", "Marca", "Modelo", 2022);
        assertEquals("ABC1D23", veiculo.getPlaca());
        assertThrows(DomainException.class, () -> new Veiculo(null, "ABC1234", "Marca", "Modelo", 2022));
    }

    @Test
    void impedeServicoInativoECalculaSnapshotDoItem() {
        Servico servico = new Servico(2L, "REV-01", "Revisao", null, new BigDecimal("50.00"),
                Duration.ofHours(2), OffsetDateTime.now(), true);
        ItemServico item = new ItemServico(8L, servico, new BigDecimal("2"));
        assertEquals(new BigDecimal("100.00"), item.getSubtotal());
        servico.desativar();
        assertThrows(DomainException.class, () -> new ItemServico(8L, servico, BigDecimal.ONE));
    }

    @Test
    void controlaEstoqueEConsumoDeMaterial() {
        Material material = new Material(4L, "OLEO-01", TipoMaterial.INSUMO, "Oleo", null,
                new BigDecimal("25.00"), "litro", new BigDecimal("5.000"), BigDecimal.ONE,
                OffsetDateTime.now(), true);
        ItemMaterial item = new ItemMaterial(8L, material, new BigDecimal("2.000"));
        item.consumir(material);
        assertEquals(new BigDecimal("3.000"), material.getQuantidadeEstoque());
        assertThrows(DomainException.class, () -> new ItemMaterial(8L, material, new BigDecimal("4.000")));
    }

    @Test
    void geraOrcamentoComValoresPorCategoriaEExigeDisponibilizacaoParaDecisao() {
        ItemServico servico = new ItemServico(1L, 8L, 3L, "Mao de obra", BigDecimal.ONE,
                new BigDecimal("100.00"), new BigDecimal("100.00"), 3600);
        ItemMaterial peca = new ItemMaterial(1L, 8L, 4L, TipoMaterial.PECA, "Filtro", BigDecimal.ONE,
                new BigDecimal("30.00"), new BigDecimal("30.00"));
        Orcamento orcamento = new Orcamento(8L, 1, List.of(servico), List.of(peca));
        assertEquals(new BigDecimal("130.00"), orcamento.getValorTotal());
        assertThrows(DomainException.class, () -> orcamento.aprovar(null));
        orcamento.disponibilizar();
        orcamento.aprovar("De acordo");
        assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
        assertThrows(DomainException.class, orcamento::disponibilizar);
    }

    @Test
    void registraTransicoesEImpossibilitaAlteracaoAposEntrega() {
        OrdemServico ordem = new OrdemServico(1L, 2L);
        ordem.setId(1L);
        ordem.enviarParaDiagnostico();
        Servico servico = new Servico(3L, "REV-02", "Revisao", null, new BigDecimal("250.00"),
                Duration.ofHours(1), OffsetDateTime.now(), true);
        ordem.adicionarItemServico(new ItemServico(1L, servico, BigDecimal.ONE));
        Orcamento orcamento = ordem.gerarOrcamento();
        ordem.disponibilizarOrcamento(orcamento);
        ordem.aprovarOrcamento(orcamento, "Aprovado");
        ordem.finalizar();
        ordem.entregar();
        assertEquals(StatusOrdemServico.ENTREGUE, ordem.getStatus());
        assertEquals(5, ordem.getHistoricoStatus().size());
        HistoricoStatus ultima = ordem.getHistoricoStatus().get(4);
        assertEquals(StatusOrdemServico.ENTREGUE, ultima.getStatusNovo());
        assertThrows(DomainException.class, () -> ordem.definirObservacoes("Alteracao tardia"));
    }

    @Test
    void falhaAoAprovarOrcamentoEmEstadoInvalidoSemAlterarAprovacao() {
        OrdemServico ordem = new OrdemServico(1L, 2L);
        assertThrows(DomainException.class, ordem::aprovarOrcamento);
        assertFalse(ordem.isOrcamentoAprovado());
    }

    @Test
    void atualizaOrcamentoPendenteQuandoItensDaOrdemMudam() {
        OrdemServico ordem = new OrdemServico(20L, "OS-20", 1L, 2L, StatusOrdemServico.RECEBIDA,
                false, BigDecimal.ZERO, OffsetDateTime.now(), null, null, null, null, null, null);
        Servico servico = new Servico(3L, "REV-02", "Revisao", null, new BigDecimal("80.00"),
                Duration.ofHours(1), OffsetDateTime.now(), true);
        ordem.enviarParaDiagnostico();
        ordem.adicionarItemServico(new ItemServico(20L, servico, BigDecimal.ONE));
        Orcamento orcamento = ordem.gerarOrcamento();
        ordem.adicionarItemServico(new ItemServico(20L, servico, BigDecimal.ONE));
        assertEquals(new BigDecimal("160.00"), orcamento.getValorTotal());
        ordem.disponibilizarOrcamento(orcamento);
        ordem.aprovarOrcamento(orcamento, null);
        assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
        assertThrows(DomainException.class,
                () -> ordem.adicionarItemServico(new ItemServico(20L, servico, BigDecimal.ONE)));
    }
}

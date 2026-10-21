package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {
    @Test void deveExecutarFluxoPrincipal() {
        var os=new OrdemServico(1L,2L);
        os.setId(1L);
        os.enviarParaDiagnostico();
        var servico = new br.com.fiap.techchallengefiap.domain.servico.model.Servico(1L, "S1", "Troca de oleo", "Descricao", new BigDecimal("250.00"), java.time.Duration.ofMinutes(60), java.time.OffsetDateTime.now(), true);
        os.adicionarItemServico(new ItemServico(1L, servico, BigDecimal.ONE));
        var orcamento = os.gerarOrcamento();
        os.disponibilizarOrcamento(orcamento);
        os.aprovarOrcamento(orcamento, "Aprovado pelo cliente");
        os.finalizar();
        os.entregar();
        assertEquals(StatusOrdemServico.ENTREGUE, os.getStatus());
    }

    @Test void naoDeveFinalizarSemAprovacao() {
        var os=new OrdemServico(1L,2L);
        assertThrows(DomainException.class, os::finalizar);
    }

    @Test void deveCancelarOsQuandoOrcamentoERejeitado() {
        var os = new OrdemServico(1L, 2L);
        os.setId(1L);
        os.enviarParaDiagnostico();
        var servico = new br.com.fiap.techchallengefiap.domain.servico.model.Servico(1L, "S1", "Troca de oleo", "Descricao", new BigDecimal("250.00"), java.time.Duration.ofMinutes(60), java.time.OffsetDateTime.now(), true);
        os.adicionarItemServico(new ItemServico(1L, servico, BigDecimal.ONE));
        var orcamento = os.gerarOrcamento();
        os.disponibilizarOrcamento(orcamento);
        os.rejeitarOrcamento(orcamento, "Cliente recusou.");
        assertEquals(StatusOrdemServico.CANCELADA, os.getStatus());
    }

    @Test void deveBloquearItensForaDoDiagnostico() {
        var os = new OrdemServico(1L, 2L);
        os.enviarParaDiagnostico();
        os.aguardarAprovacao(new BigDecimal("100.00"));
        assertThrows(DomainException.class, () -> os.adicionarItemServico(new ItemServico(1L, new br.com.fiap.techchallengefiap.domain.servico.model.Servico("S1", "Troca", "Descrição", new BigDecimal("50.00"), java.time.Duration.ofMinutes(30)), new BigDecimal("1"))));
    }
}

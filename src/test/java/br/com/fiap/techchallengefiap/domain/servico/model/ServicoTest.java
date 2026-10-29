package br.com.fiap.techchallengefiap.domain.servico.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicoTest {
    private static final OffsetDateTime DATA = OffsetDateTime.parse("2025-01-02T10:15:30-03:00");

    @Test
    void criaServicoComDadosValidosEArredondaValorBase() {
        Servico servico = new Servico(4L, " SVC-1 ", " Revisao ", " descricao ",
                new BigDecimal("12.345"), Duration.ofMinutes(30), DATA, true);

        assertEquals("SVC-1", servico.getCodigo());
        assertEquals("Revisao", servico.getNome());
        assertEquals("descricao", servico.getDescricao());
        assertEquals(new BigDecimal("12.35"), servico.getValorBase());
        assertEquals(Duration.ofMinutes(30), servico.getTempoEstimado());
        assertEquals(DATA, servico.getDataCadastro());
    }

    @Test
    void rejeitaCodigoOuNomeEmBranco() {
        assertThrows(DomainException.class, () -> new Servico(" ", "Servico", null, BigDecimal.ONE, Duration.ofMinutes(1)));
        assertThrows(DomainException.class, () -> new Servico("S1", null, null, BigDecimal.ONE, Duration.ofMinutes(1)));
    }

    @Test
    void rejeitaValorNuloOuNegativo() {
        assertThrows(DomainException.class, () -> new Servico("S1", "Servico", null, null, Duration.ofMinutes(1)));
        assertThrows(DomainException.class,
                () -> new Servico("S1", "Servico", null, new BigDecimal("-0.01"), Duration.ofMinutes(1)));
    }

    @Test
    void rejeitaTempoNuloZeroOuNegativo() {
        assertThrows(DomainException.class, () -> new Servico("S1", "Servico", null, BigDecimal.ONE, null));
        assertThrows(DomainException.class, () -> new Servico("S1", "Servico", null, BigDecimal.ONE, Duration.ZERO));
        assertThrows(DomainException.class, () -> new Servico("S1", "Servico", null, BigDecimal.ONE, Duration.ofSeconds(-1)));
    }

    @Test
    void alteraAtivacaoDoServico() {
        Servico servico = new Servico(1L, "S1", "Servico", null, BigDecimal.ONE, Duration.ofMinutes(1), DATA, true);
        servico.desativar();
        assertFalse(servico.isAtivo());
        servico.ativar();
        assertTrue(servico.isAtivo());
    }
}

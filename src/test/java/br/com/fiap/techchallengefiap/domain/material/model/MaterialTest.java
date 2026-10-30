package br.com.fiap.techchallengefiap.domain.material.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MaterialTest {
    private static final OffsetDateTime DATA = OffsetDateTime.parse("2025-01-02T10:15:30-03:00");

    private Material material() {
        return new Material(2L, " MAT-1 ", TipoMaterial.PECA, " Filtro ", " descricao ",
                new BigDecimal("10.125"), " un ", new BigDecimal("5.000"), BigDecimal.ONE, DATA, true);
    }

    @Test
    void criaMaterialComDadosValidosEArredondaValorUnitario() {
        Material material = material();

        assertEquals("MAT-1", material.getCodigo());
        assertEquals("Filtro", material.getNome());
        assertEquals("descricao", material.getDescricao());
        assertEquals(new BigDecimal("10.13"), material.getValorUnitario());
        assertEquals("un", material.getUnidadeMedida());
        assertEquals(DATA, material.getDataCadastro());
    }

    @Test
    void rejeitaCamposObrigatoriosOuValoresNegativos() {
        assertThrows(DomainException.class, () -> new Material(" ", TipoMaterial.PECA, "x", null, BigDecimal.ONE, "un", BigDecimal.ONE, BigDecimal.ZERO));
        assertThrows(NullPointerException.class, () -> new Material("M", null, "x", null, BigDecimal.ONE, "un", BigDecimal.ONE, BigDecimal.ZERO));
        assertThrows(DomainException.class, () -> new Material("M", TipoMaterial.PECA, " ", null, BigDecimal.ONE, "un", BigDecimal.ONE, BigDecimal.ZERO));
        assertThrows(DomainException.class, () -> new Material("M", TipoMaterial.PECA, "x", null, BigDecimal.ONE, " ", BigDecimal.ONE, BigDecimal.ZERO));
        assertThrows(DomainException.class, () -> new Material("M", TipoMaterial.PECA, "x", null, new BigDecimal("-1"), "un", BigDecimal.ONE, BigDecimal.ZERO));
        assertThrows(DomainException.class, () -> new Material("M", TipoMaterial.PECA, "x", null, BigDecimal.ONE, "un", new BigDecimal("-1"), BigDecimal.ZERO));
        assertThrows(DomainException.class, () -> new Material("M", TipoMaterial.PECA, "x", null, BigDecimal.ONE, "un", BigDecimal.ONE, new BigDecimal("-1")));
    }

    @Test
    void validaDisponibilidadeComEstoqueSuficiente() {
        assertDoesNotThrow(() -> material().validarDisponibilidade(new BigDecimal("5.000")));
    }

    @Test
    void rejeitaQuantidadeNaoPositivaEstoqueInsuficienteOuMaterialInativo() {
        Material material = material();
        assertThrows(DomainException.class, () -> material.validarDisponibilidade(BigDecimal.ZERO));
        assertThrows(DomainException.class, () -> material.validarDisponibilidade(new BigDecimal("5.001")));
        material.desativar();
        assertThrows(DomainException.class, () -> material.validarDisponibilidade(BigDecimal.ONE));
    }

    @Test
    void consomeEstoqueSomenteAposValidacao() {
        Material material = material();
        material.consumir(new BigDecimal("2.000"));
        assertEquals(new BigDecimal("3.000"), material.getQuantidadeEstoque());
        assertThrows(DomainException.class, () -> material.consumir(new BigDecimal("4.000")));
        assertEquals(new BigDecimal("3.000"), material.getQuantidadeEstoque());
    }

    @Test
    void registraEntradaSomenteComQuantidadePositiva() {
        Material material = material();
        material.registrarEntrada(new BigDecimal("2.500"));
        assertEquals(new BigDecimal("7.500"), material.getQuantidadeEstoque());
        assertThrows(DomainException.class, () -> material.registrarEntrada(BigDecimal.ZERO));
        assertThrows(DomainException.class, () -> material.registrarEntrada(new BigDecimal("-1")));
        assertThrows(DomainException.class, () -> material.registrarEntrada(null));
        assertEquals(new BigDecimal("7.500"), material.getQuantidadeEstoque());
    }

    @Test
    void alteraAtivacaoDoMaterial() {
        Material material = material();
        material.desativar();
        assertFalse(material.isAtivo());
        material.ativar();
        assertTrue(material.isAtivo());
    }
}

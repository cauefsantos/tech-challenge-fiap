package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemMaterialTest {
    @Test
    void criaSnapshotDeTipoPrecoEDescricaoDoMaterial() {
        Material material = OrdemServicoFixture.material(TipoMaterial.INSUMO);
        ItemMaterial item = new ItemMaterial(10L, material, new BigDecimal("2.000"));
        material.desativar();

        assertEquals(4L, item.getMaterialId());
        assertEquals(TipoMaterial.INSUMO, item.getTipo());
        assertEquals("Filtro", item.getDescricao());
        assertEquals(new BigDecimal("25.00"), item.getValorUnitario());
        assertEquals(new BigDecimal("50.00"), item.getSubtotal());
    }

    @Test
    void rejeitaMaterialInexistenteQuantidadeInvalidaOuIndisponivel() {
        assertThrows(DomainException.class, () -> new ItemMaterial(1L, null, BigDecimal.ONE));
        Material semId = new Material("M", TipoMaterial.PECA, "Filtro", null, BigDecimal.ONE,
                "un", BigDecimal.ONE, BigDecimal.ZERO);
        assertThrows(DomainException.class, () -> new ItemMaterial(1L, semId, BigDecimal.ONE));
        Material material = OrdemServicoFixture.material(TipoMaterial.PECA);
        assertThrows(DomainException.class, () -> new ItemMaterial(1L, material, BigDecimal.ZERO));
        assertThrows(DomainException.class, () -> new ItemMaterial(1L, material, new BigDecimal("11")));
        material.desativar();
        assertThrows(DomainException.class, () -> new ItemMaterial(1L, material, BigDecimal.ONE));
    }

    @Test
    void calculaSubtotalDoItemMaterial() {
        ItemMaterial item = new ItemMaterial(1L, OrdemServicoFixture.material(TipoMaterial.PECA),
                new BigDecimal("1.25"));

        assertEquals(new BigDecimal("31.25"), item.getSubtotal());
    }

    @Test
    void rejeitaReconstrucaoComDadosOuSubtotalInvalidos() {
        assertThrows(DomainException.class, () -> new ItemMaterial(1L, 1L, null, TipoMaterial.PECA, "x", BigDecimal.ONE, BigDecimal.ONE, null));
        assertThrows(DomainException.class, () -> new ItemMaterial(1L, 1L, 1L, null, "x", BigDecimal.ONE, BigDecimal.ONE, null));
        assertThrows(DomainException.class, () -> new ItemMaterial(1L, 1L, 1L, TipoMaterial.PECA, "x", BigDecimal.ZERO, BigDecimal.ONE, null));
        assertThrows(DomainException.class, () -> new ItemMaterial(1L, 1L, 1L, TipoMaterial.PECA, "x", BigDecimal.ONE, new BigDecimal("-1"), null));
        assertThrows(DomainException.class, () -> new ItemMaterial(1L, 1L, 1L, TipoMaterial.PECA, "x", BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("2")));
    }

    @Test
    void rejeitaConsumoComMaterialDiferenteDoItem() {
        ItemMaterial item = new ItemMaterial(1L, OrdemServicoFixture.material(TipoMaterial.PECA), BigDecimal.ONE);
        Material outro = new Material(5L, "OUTRO", TipoMaterial.PECA, "Outro", null, BigDecimal.ONE,
                "un", BigDecimal.TEN, BigDecimal.ZERO, OrdemServicoFixture.DATA, true);

        assertThrows(DomainException.class, () -> item.consumir(null));
        assertThrows(DomainException.class, () -> item.consumir(outro));
        assertEquals(new BigDecimal("10"), outro.getQuantidadeEstoque());
    }
}

package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.orcamento.model.Orcamento;
import br.com.fiap.techchallengefiap.domain.servico.model.Servico;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

public final class OrdemServicoFixture {
    public static final OffsetDateTime DATA = OffsetDateTime.parse("2025-01-02T10:15:30-03:00");

    private OrdemServicoFixture() {
    }

    public static OrdemServico novaOrdemPersistida() {
        return new OrdemServico(10L, "OS-10", 1L, 2L, StatusOrdemServico.RECEBIDA,
                false, BigDecimal.ZERO, DATA, null, null, null, null, null, null);
    }

    public static Servico servico() {
        return new Servico(3L, "SVC-1", "Troca de oleo", "Revisao", new BigDecimal("100.00"),
                Duration.ofMinutes(60), DATA, true);
    }

    public static Material material(TipoMaterial tipo) {
        return new Material(4L, "MAT-1", tipo, "Filtro", null, new BigDecimal("25.00"),
                "un", new BigDecimal("10.000"), BigDecimal.ONE, DATA, true);
    }

    public static ItemServico itemServico() {
        return new ItemServico(10L, servico(), BigDecimal.ONE);
    }

    public static ItemMaterial itemMaterial(TipoMaterial tipo) {
        return new ItemMaterial(10L, material(tipo), new BigDecimal("2.000"));
    }

    public static OrdemServico emDiagnosticoComItens() {
        OrdemServico ordem = novaOrdemPersistida();
        ordem.enviarParaDiagnostico();
        ordem.adicionarItemServico(itemServico());
        ordem.adicionarItemMaterial(itemMaterial(TipoMaterial.PECA));
        return ordem;
    }

    public static Orcamento disponibilizado() {
        OrdemServico ordem = emDiagnosticoComItens();
        Orcamento orcamento = ordem.gerarOrcamento();
        ordem.disponibilizarOrcamento(orcamento);
        return orcamento;
    }
}

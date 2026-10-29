package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.orcamento.model.Orcamento;
import br.com.fiap.techchallengefiap.domain.orcamento.model.StatusOrcamento;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

final class OrdemServicoUseCaseFixture {
    static final OffsetDateTime DATA = OffsetDateTime.parse("2024-01-01T10:00:00Z");
    static final Long OS_ID = 10L;
    static final Long CLIENTE_ID = 20L;
    static final Long VEICULO_ID = 30L;

    private OrdemServicoUseCaseFixture() {}

    static OrdemServico os(StatusOrdemServico status) {
        return new OrdemServico(OS_ID, "ABC12345", CLIENTE_ID, VEICULO_ID, status, false,
                BigDecimal.ZERO, DATA, null, null, null, null, null, null);
    }

    static OrdemServico osAprovacao() {
        OrdemServico os = new OrdemServico(OS_ID, "ABC12345", CLIENTE_ID, VEICULO_ID,
                StatusOrdemServico.AGUARDANDO_APROVACAO, false, new BigDecimal("10.00"),
                DATA, null, null, null, null, null, null);
        os.carregarMateriaisPersistidos(List.of(new ItemMaterial(101L, OS_ID, 50L,
                TipoMaterial.PECA, "Filtro", BigDecimal.ONE, new BigDecimal("10.00"), new BigDecimal("10.00"))));
        Orcamento orcamento = new Orcamento(200L, OS_ID, 1, BigDecimal.ZERO, new BigDecimal("10.00"),
                BigDecimal.ZERO, new BigDecimal("10.00"), StatusOrcamento.DISPONIBILIZADO, DATA, null, null);
        os.carregarOrcamentosPersistidos(List.of(orcamento));
        return os;
    }

    static OrdemServico osFinalizada() {
        OrdemServico os = new OrdemServico(OS_ID, "ABC12345", CLIENTE_ID, VEICULO_ID,
                StatusOrdemServico.FINALIZADA, true, BigDecimal.TEN, DATA, null, null,
                DATA, DATA.plusSeconds(120), null, null);
        os.carregarItensPersistidos(List.of(new ItemServico(11L, OS_ID, 40L,
                "Reparo", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, 60)));
        return os;
    }
}

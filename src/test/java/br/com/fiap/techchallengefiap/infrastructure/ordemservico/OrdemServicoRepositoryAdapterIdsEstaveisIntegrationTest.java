package br.com.fiap.techchallengefiap.infrastructure.ordemservico;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.orcamento.model.StatusOrcamento;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import br.com.fiap.techchallengefiap.domain.servico.model.Servico;
import br.com.fiap.techchallengefiap.domain.veiculo.model.Veiculo;
import br.com.fiap.techchallengefiap.infrastructure.cliente.ClienteRepositoryAdapter;
import br.com.fiap.techchallengefiap.infrastructure.material.MaterialRepositoryAdapter;
import br.com.fiap.techchallengefiap.infrastructure.servico.ServicoRepositoryAdapter;
import br.com.fiap.techchallengefiap.infrastructure.veiculo.VeiculoRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Garante que salvar a OS preserva os ids de itens e orcamentos ja persistidos, pois a API usa esses ids
 * nas operacoes seguintes (disponibilizar/aprovar orcamento, remover item).
 */
@SpringBootTest
class OrdemServicoRepositoryAdapterIdsEstaveisIntegrationTest {
    @Autowired
    private OrdemServicoRepository repository;
    @Autowired
    private ClienteRepositoryAdapter clienteRepository;
    @Autowired
    private VeiculoRepositoryAdapter veiculoRepository;
    @Autowired
    private ServicoRepositoryAdapter servicoRepository;
    @Autowired
    private MaterialRepositoryAdapter materialRepository;

    @Test
    void preservaIdsDeItensEOrcamentosEntreSalvamentos() {
        var cliente = clienteRepository.salvar(new Cliente(TipoPessoa.FISICA, "86420975310", "Paula Reis", null, null));
        var veiculo = veiculoRepository.salvar(new Veiculo(cliente.getId(), "ORC2B34", "Fiat", "Argo", 2022));
        var servico = servicoRepository.salvar(new Servico("SV-IDS-1", "Alinhamento", null, new BigDecimal("100.00"),
                Duration.ofMinutes(30)));
        var material = materialRepository.salvar(new Material("MAT-IDS-1", TipoMaterial.PECA, "Pastilha", null,
                new BigDecimal("40.00"), "un", new BigDecimal("10"), BigDecimal.ONE));

        var os = repository.salvar(new OrdemServico(cliente.getId(), veiculo.getId()));
        os.enviarParaDiagnostico();
        os.adicionarItemServico(new ItemServico(os.getId(), servico, BigDecimal.ONE));
        os.adicionarItemServico(new ItemServico(os.getId(), servico, new BigDecimal("2")));
        os.adicionarItemMaterial(new ItemMaterial(os.getId(), material, BigDecimal.ONE));
        os.gerarOrcamento();
        var gerada = repository.salvar(os);

        Long orcamentoId = gerada.getOrcamentos().getFirst().getId();
        Long itemMantido = gerada.getItensServico().get(0).getId();
        Long itemRemovido = gerada.getItensServico().get(1).getId();
        Long itemMaterial = gerada.getItensMaterial().getFirst().getId();
        int historicoInicial = gerada.getHistoricoStatus().size();
        assertNotNull(orcamentoId);
        assertNotNull(itemMantido);

        gerada.removerItemServico(itemRemovido);
        var aposRemocao = repository.salvar(gerada);

        assertEquals(List.of(itemMantido), aposRemocao.getItensServico().stream().map(ItemServico::getId).toList());
        assertEquals(itemMaterial, aposRemocao.getItensMaterial().getFirst().getId());
        assertEquals(orcamentoId, aposRemocao.getOrcamentos().getFirst().getId());
        assertEquals(new BigDecimal("140.00"), aposRemocao.getOrcamentos().getFirst().getValorTotal());
        assertEquals(historicoInicial, aposRemocao.getHistoricoStatus().size());

        aposRemocao.disponibilizarOrcamento(aposRemocao.getOrcamentos().getFirst());
        var disponibilizada = repository.salvar(aposRemocao);
        assertEquals(orcamentoId, disponibilizada.getOrcamentos().getFirst().getId());
        assertEquals(StatusOrcamento.DISPONIBILIZADO, disponibilizada.getOrcamentos().getFirst().getStatus());

        disponibilizada.aprovarOrcamento(orcamentoId, "Aprovado pelo cliente");
        var aprovada = repository.salvar(disponibilizada);

        assertEquals(StatusOrdemServico.EM_EXECUCAO, aprovada.getStatus());
        assertEquals(1, aprovada.getOrcamentos().size());
        assertEquals(orcamentoId, aprovada.getOrcamentos().getFirst().getId());
        assertEquals(StatusOrcamento.APROVADO, aprovada.getOrcamentos().getFirst().getStatus());
        assertEquals("Aprovado pelo cliente", aprovada.getOrcamentos().getFirst().getObservacaoResposta());
        assertEquals(historicoInicial + 2, aprovada.getHistoricoStatus().size());
    }
}

package br.com.fiap.techchallengefiap.infrastructure.ordemservico;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrdemServicoRepositoryAdapterIntegrationTest {

    @Autowired
    private OrdemServicoRepository repository;

    @Autowired
    private ServicoRepositoryAdapter servicoRepository;

    @Autowired
    private MaterialRepositoryAdapter materialRepository;

    @Autowired
    private ClienteRepositoryAdapter clienteRepository;

    @Autowired
    private VeiculoRepositoryAdapter veiculoRepository;

    @Test
    void devePersistirAgregadoCompletoDaOrdemDeServico() {
        var cliente = clienteRepository.salvar(new Cliente(TipoPessoa.FISICA, "52998224725", "Ana Souza", "ana@email.com", "11999999999"));
        var veiculo = veiculoRepository.salvar(new Veiculo(cliente.getId(), "ABC1D23", "Chevrolet", "Onix", 2023));
        var servico = servicoRepository.salvar(new Servico("SV-001", "Balanceamento", "Alinhamento e balanceamento", new BigDecimal("150.00"), Duration.ofMinutes(60)));
        var material = materialRepository.salvar(new Material("MAT-001", TipoMaterial.PECA, "Filtro de ar", "Filtro de cabine", new BigDecimal("25.00"), "un", new BigDecimal("10"), new BigDecimal("2")));

        var os = repository.salvar(new OrdemServico(cliente.getId(), veiculo.getId()));
        os.enviarParaDiagnostico();
        os.definirDescricaoProblema("Veiculo com vibraçao e ruído.");
        os.adicionarItemServico(new ItemServico(os.getId(), servico, new BigDecimal("1")));
        os.adicionarItemMaterial(new ItemMaterial(os.getId(), material, new BigDecimal("2")));

        var orcamento = os.gerarOrcamento();
        os.disponibilizarOrcamento(orcamento);
        os.aprovarOrcamento(orcamento, "Aprovado pelo cliente");
        os.finalizar();
        os.entregar();

        var salva = repository.salvar(os);
        var persistida = repository.buscarPorId(salva.getId()).orElseThrow();

        assertEquals(StatusOrdemServico.ENTREGUE, persistida.getStatus());
        assertEquals(1, persistida.getItensServico().size());
        assertEquals(1, persistida.getItensMaterial().size());
        assertEquals(1, persistida.getOrcamentos().size());
        assertFalse(persistida.getHistoricoStatus().isEmpty());
        assertEquals(new BigDecimal("200.00"), persistida.getValorTotal());
    }
}

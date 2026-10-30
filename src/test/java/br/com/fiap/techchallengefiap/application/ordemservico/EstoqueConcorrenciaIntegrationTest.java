package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.application.cliente.GerenciarClienteUseCase;
import br.com.fiap.techchallengefiap.application.material.GerenciarMaterialUseCase;
import br.com.fiap.techchallengefiap.application.servico.GerenciarServicoUseCase;
import br.com.fiap.techchallengefiap.application.veiculo.GerenciarVeiculoUseCase;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.infrastructure.material.MaterialRepositoryAdapter;
import br.com.fiap.techchallengefiap.infrastructure.ordemservico.OrdemServicoRepositoryAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

/** Cada chamada ao caso de uso abre sua propria transacao; o teste nao compartilha uma transacao. */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EstoqueConcorrenciaIntegrationTest {
    @Autowired GerenciarClienteUseCase clientes;
    @Autowired GerenciarVeiculoUseCase veiculos;
    @Autowired GerenciarServicoUseCase servicos;
    @Autowired GerenciarMaterialUseCase materiais;
    @Autowired CriarOrdemServicoUseCase criar;
    @Autowired OrdemServicoFluxoUseCase fluxo;
    @MockitoSpyBean MaterialRepositoryAdapter materialRepository;
    @MockitoSpyBean OrdemServicoRepositoryAdapter osRepository;

    private Long servicoId;

    @BeforeAll
    void cadastrar() {
        clientes.criar(TipoPessoa.FISICA, "31415926590", "Cliente concorrencia", null, null);
        veiculos.criar("31415926590", "CON1C23", "Fiat", "Uno", 2020);
        servicoId = servicos.criar("CON-" + UUID.randomUUID(), "Troca", null, BigDecimal.TEN, 60L).getId();
    }

    @Test
    void duasOrdensDisputamSaldoEEstoqueInsuficienteImpedeSegundaAprovacao() throws Exception {
        var material = novoMaterial("10");
        var primeira = prepararOrdem(material, "7");
        var segunda = prepararOrdem(material, "7");

        executarConcorrentes(material.getId(), primeira, segunda, false);

        assertSaldo(material.getId(), "3");
        assertEquals(StatusOrdemServico.EM_EXECUCAO, osRepository.buscarPorId(primeira.osId()).orElseThrow().getStatus());
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, osRepository.buscarPorId(segunda.osId()).orElseThrow().getStatus());
    }

    @Test
    void duasOrdensComSaldoSuficienteDescontamAsDuasQuantidades() throws Exception {
        var material = novoMaterial("20");
        executarConcorrentes(material.getId(), prepararOrdem(material, "7"), prepararOrdem(material, "7"), true);
        assertSaldo(material.getId(), "6");
    }

    @Test
    void duasTentativasDaMesmaOrdemNaoBaixamEstoqueDuasVezes() throws Exception {
        var material = novoMaterial("20");
        var ordem = prepararOrdem(material, "7");
        executarConcorrentes(material.getId(), ordem, ordem, false);
        assertSaldo(material.getId(), "13");
    }

    @Test
    void desativacaoAdministrativaNaoSobrescreveSaldoDaAprovacao() throws Exception {
        var material = novoMaterial("10");
        var ordem = prepararOrdem(material, "7");
        var bloqueada = new CountDownLatch(1);
        var liberar = new CountDownLatch(1);
        var segundaLeitura = new CountDownLatch(1);
        pausarPrimeiraLeitura(material.getId(), bloqueada, liberar, segundaLeitura);

        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> aprovacao = executor.submit(() -> aprovar(ordem));
            Future<Material> desativacao = null;
            try {
                aguardar(bloqueada);
                desativacao = executor.submit(() -> materiais.alterarAtivo(material.getId(), false));
                aguardar(segundaLeitura);
                Future<Material> pendente = desativacao;
                assertThrows(TimeoutException.class, () -> pendente.get(300, TimeUnit.MILLISECONDS));
            } finally {
                liberar.countDown();
            }
            assertTrue(aprovacao.get(10, TimeUnit.SECONDS));
            assertNotNull(desativacao);
            assertFalse(desativacao.get(10, TimeUnit.SECONDS).isAtivo());
        }
        assertSaldo(material.getId(), "3");
    }

    @Test
    void falhaAoSalvarOrdemDesfazBaixaEPermiteNovaTentativa() {
        var material = novoMaterial("10");
        var ordem = prepararOrdem(material, "7");
        doAnswer(inv -> {
            throw new DataAccessResourceFailureException("Falha simulada na persistencia da OS");
        }).doCallRealMethod().when(osRepository).salvar(any());

        assertThrows(DataAccessResourceFailureException.class, () -> aprovar(ordem));
        assertSaldo(material.getId(), "10");
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, osRepository.buscarPorId(ordem.osId()).orElseThrow().getStatus());
        assertTrue(aprovar(ordem));
        assertSaldo(material.getId(), "3");
    }

    private void executarConcorrentes(Long materialId, Ordem primeira, Ordem segunda, boolean segundaAprovada) throws Exception {
        var bloqueada = new CountDownLatch(1);
        var liberar = new CountDownLatch(1);
        var segundaTentativa = new CountDownLatch(1);
        pausarPrimeiraLeitura(materialId, bloqueada, liberar, new CountDownLatch(1));
        var tentativas = new AtomicInteger();
        doAnswer(inv -> {
            if (tentativas.incrementAndGet() == 2) segundaTentativa.countDown();
            return inv.callRealMethod();
        }).when(osRepository).buscarPorIdParaAtualizacao(any());

        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> a = executor.submit(() -> aprovar(primeira));
            Future<Boolean> b = null;
            try {
                aguardar(bloqueada);
                b = executor.submit(() -> aprovar(segunda));
                aguardar(segundaTentativa);
                Future<Boolean> pendente = b;
                assertThrows(TimeoutException.class, () -> pendente.get(300, TimeUnit.MILLISECONDS));
            } finally {
                liberar.countDown();
            }
            assertTrue(a.get(10, TimeUnit.SECONDS));
            assertNotNull(b);
            assertEquals(segundaAprovada, b.get(10, TimeUnit.SECONDS));
        }
    }

    private void pausarPrimeiraLeitura(Long id, CountDownLatch bloqueada, CountDownLatch liberar,
                                       CountDownLatch segundaLeitura) {
        var chamadas = new AtomicInteger();
        doAnswer(inv -> {
            boolean primeira = chamadas.incrementAndGet() == 1;
            if (!primeira) segundaLeitura.countDown();
            var material = inv.callRealMethod();
            if (primeira) {
                bloqueada.countDown();
                aguardar(liberar);
            }
            return material;
        }).when(materialRepository).buscarPorIdParaAtualizacao(eq(id));
    }

    private static void aguardar(CountDownLatch latch) throws InterruptedException {
        assertTrue(latch.await(10, TimeUnit.SECONDS), "A operacao concorrente nao atingiu o ponto esperado");
    }

    private boolean aprovar(Ordem ordem) {
        try {
            fluxo.aprovarOrcamento(ordem.osId(), ordem.orcamentoId(), "Aprovado");
            return true;
        } catch (DomainException rejeitada) {
            return false;
        }
    }

    private Material novoMaterial(String saldo) {
        return materiais.criar("CON-" + UUID.randomUUID(), TipoMaterial.PECA, "Filtro", null,
                BigDecimal.TEN, "un", new BigDecimal(saldo), BigDecimal.ZERO);
    }

    private Ordem prepararOrdem(Material material, String quantidade) {
        Long id = criar.executar(new CriarOrdemServicoCommand("31415926590", "CON1C23")).getId();
        fluxo.enviarParaDiagnostico(id);
        fluxo.adicionarItemServico(id, servicoId, BigDecimal.ONE);
        fluxo.adicionarItemMaterial(id, material.getId(), new BigDecimal(quantidade));
        Long orcamento = fluxo.gerarOrcamento(id).getId();
        fluxo.disponibilizarOrcamento(id, orcamento);
        return new Ordem(id, orcamento);
    }

    private void assertSaldo(Long id, String esperado) {
        assertEquals(0, new BigDecimal(esperado).compareTo(materiais.buscar(id).getQuantidadeEstoque()));
    }

    private record Ordem(Long osId, Long orcamentoId) { }
}

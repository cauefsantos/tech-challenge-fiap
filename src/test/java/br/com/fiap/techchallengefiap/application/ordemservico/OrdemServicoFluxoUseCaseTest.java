package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.material.model.Material;
import br.com.fiap.techchallengefiap.domain.material.model.TipoMaterial;
import br.com.fiap.techchallengefiap.domain.material.repository.MaterialRepository;
import br.com.fiap.techchallengefiap.domain.orcamento.model.Orcamento;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemMaterial;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.ItemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import br.com.fiap.techchallengefiap.domain.servico.model.Servico;
import br.com.fiap.techchallengefiap.domain.servico.repository.ServicoRepository;
import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static br.com.fiap.techchallengefiap.application.ordemservico.OrdemServicoUseCaseFixture.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoFluxoUseCaseTest {
    @Mock OrdemServicoRepository repository;
    @Mock ServicoRepository servicoRepository;
    @Mock MaterialRepository materialRepository;
    @InjectMocks OrdemServicoFluxoUseCase useCase;

    @Test
    void enviaParaDiagnosticoEConcluiPersistindoOs() {
        OrdemServico os = os(StatusOrdemServico.RECEBIDA);
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, useCase.enviarParaDiagnostico(OS_ID).getStatus());
        verify(repository).salvar(os);
    }

    @Test
    void registraDiagnosticoEConcluiDiagnosticoPersistindoOs() {
        OrdemServico os = os(StatusOrdemServico.EM_DIAGNOSTICO);
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals("Diagnóstico concluído", useCase.registrarDiagnostico(OS_ID, " Diagnóstico concluído ").getDiagnostico());
        assertSame(os, useCase.concluirDiagnostico(OS_ID));
        verify(repository, times(2)).salvar(os);
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, os.getStatus());
    }

    @Test
    void adicionaERemoveItemServicoQuandoServicoExiste() {
        OrdemServico os = os(StatusOrdemServico.EM_DIAGNOSTICO);
        Servico servico = servico();
        ItemServico itemPersistido = new ItemServico(41L, OS_ID, 40L, "Reparo", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, 60);
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(servicoRepository.buscarPorId(40L)).thenReturn(Optional.of(servico));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(1, useCase.adicionarItemServico(OS_ID, 40L, BigDecimal.ONE).getItensServico().size());
        os.carregarItensPersistidos(List.of(itemPersistido));
        assertTrue(useCase.removerItemServico(OS_ID, 41L).getItensServico().isEmpty());
        verify(repository, times(2)).salvar(os);
    }

    @Test
    void informaServicoNaoEncontrado() {
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os(StatusOrdemServico.EM_DIAGNOSTICO)));
        when(servicoRepository.buscarPorId(40L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.adicionarItemServico(OS_ID, 40L, BigDecimal.ONE));
        verify(repository, never()).salvar(any());
    }

    @Test
    void adicionaERemoveItemMaterialQuandoMaterialExiste() {
        OrdemServico os = os(StatusOrdemServico.EM_DIAGNOSTICO);
        Material material = material(50L, BigDecimal.TEN);
        ItemMaterial itemPersistido = new ItemMaterial(51L, OS_ID, 50L, TipoMaterial.PECA, "Filtro", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN);
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(materialRepository.buscarPorId(50L)).thenReturn(Optional.of(material));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(1, useCase.adicionarItemMaterial(OS_ID, 50L, BigDecimal.ONE).getItensMaterial().size());
        os.carregarMateriaisPersistidos(List.of(itemPersistido));
        assertTrue(useCase.removerItemMaterial(OS_ID, 51L).getItensMaterial().isEmpty());
        verify(repository, times(2)).salvar(os);
    }

    @Test
    void informaMaterialNaoEncontrado() {
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os(StatusOrdemServico.EM_DIAGNOSTICO)));
        when(materialRepository.buscarPorId(50L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.adicionarItemMaterial(OS_ID, 50L, BigDecimal.ONE));
        verify(repository, never()).salvar(any());
    }

    @Test
    void listaOrdensDoRepositorio() {
        List<OrdemServico> ordens = List.of(os(StatusOrdemServico.RECEBIDA));
        when(repository.listar()).thenReturn(ordens);
        assertSame(ordens, useCase.listar());
    }

    @Test
    void geraOrcamentoEPersisteOs() {
        OrdemServico os = os(StatusOrdemServico.EM_DIAGNOSTICO);
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        Orcamento orcamento = useCase.gerarOrcamento(OS_ID);
        assertEquals(1, orcamento.getVersao());
        verify(repository).salvar(os);
    }

    @Test
    void geraOrcamentoDevolvendoOrcamentoPersistidoComId() {
        OrdemServico os = os(StatusOrdemServico.EM_DIAGNOSTICO);
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(repository.salvar(any())).thenAnswer(inv -> {
            OrdemServico salva = inv.getArgument(0);
            salva.getOrcamentos().forEach(item -> item.setId(300L));
            return salva;
        });

        assertEquals(300L, useCase.gerarOrcamento(OS_ID).getId());
    }

    @Test
    void disponibilizaOrcamentoEncontradoEPersisteOs() {
        OrdemServico os = new OrdemServico(OS_ID, "ABC12345", CLIENTE_ID, VEICULO_ID, StatusOrdemServico.EM_DIAGNOSTICO, false,
                BigDecimal.TEN, DATA, null, null, null, null, null, null);
        Orcamento orcamento = new Orcamento(200L, OS_ID, 1, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ZERO,
                BigDecimal.TEN, br.com.fiap.techchallengefiap.domain.orcamento.model.StatusOrcamento.PENDENTE, DATA, null, null);
        os.carregarOrcamentosPersistidos(List.of(orcamento));
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, useCase.disponibilizarOrcamento(OS_ID, 200L).getStatus());
        verify(repository).salvar(os);
        assertEquals(br.com.fiap.techchallengefiap.domain.orcamento.model.StatusOrcamento.DISPONIBILIZADO, orcamento.getStatus());
    }

    @Test
    void informaOrcamentoNaoEncontradoNaOs() {
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os(StatusOrdemServico.EM_DIAGNOSTICO)));
        assertThrows(ResourceNotFoundException.class, () -> useCase.disponibilizarOrcamento(OS_ID, 99L));
        verify(repository, never()).salvar(any());
    }

    @Test
    void rejeitaOrcamentoECancelaOsPersistindoResultado() {
        OrdemServico os = osAprovacao();
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        OrdemServico resultado = useCase.rejeitarOrcamento(OS_ID, 200L, "Não aprovado");
        assertEquals(StatusOrdemServico.CANCELADA, resultado.getStatus());
        verify(repository).salvar(os);
    }

    @Test
    void finalizaEEntregaOsPersistindoResultado() {
        OrdemServico paraFinalizar = new OrdemServico(OS_ID, "ABC12345", CLIENTE_ID, VEICULO_ID,
                StatusOrdemServico.EM_EXECUCAO, true, BigDecimal.TEN, DATA, null, null, DATA, null, null, null);
        paraFinalizar.carregarItensPersistidos(List.of(new ItemServico(11L, OS_ID, 40L, "Reparo", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, 60)));
        OrdemServico finalizada = new OrdemServico(OS_ID, "ABC12345", CLIENTE_ID, VEICULO_ID,
                StatusOrdemServico.FINALIZADA, true, BigDecimal.TEN, DATA, null, null, DATA,
                DATA.plusSeconds(60), null, null);
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(paraFinalizar), Optional.of(finalizada));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(StatusOrdemServico.FINALIZADA, useCase.finalizar(OS_ID).getStatus());
        assertEquals(StatusOrdemServico.ENTREGUE, useCase.entregar(OS_ID).getStatus());
        verify(repository, times(2)).salvar(any());
    }

    @Test
    void informaOsNaoEncontradaParaCadaOperacao() {
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.enviarParaDiagnostico(OS_ID));
        assertThrows(ResourceNotFoundException.class, () -> useCase.registrarDiagnostico(OS_ID, "texto"));
        assertThrows(ResourceNotFoundException.class, () -> useCase.concluirDiagnostico(OS_ID));
        assertThrows(ResourceNotFoundException.class, () -> useCase.adicionarItemServico(OS_ID, 1L, BigDecimal.ONE));
        assertThrows(ResourceNotFoundException.class, () -> useCase.removerItemServico(OS_ID, 1L));
        assertThrows(ResourceNotFoundException.class, () -> useCase.adicionarItemMaterial(OS_ID, 1L, BigDecimal.ONE));
        assertThrows(ResourceNotFoundException.class, () -> useCase.removerItemMaterial(OS_ID, 1L));
        assertThrows(ResourceNotFoundException.class, () -> useCase.gerarOrcamento(OS_ID));
        assertThrows(ResourceNotFoundException.class, () -> useCase.disponibilizarOrcamento(OS_ID, 1L));
        assertThrows(ResourceNotFoundException.class, () -> useCase.aprovarOrcamento(OS_ID, 1L, null));
        assertThrows(ResourceNotFoundException.class, () -> useCase.rejeitarOrcamento(OS_ID, 1L, null));
        assertThrows(ResourceNotFoundException.class, () -> useCase.finalizar(OS_ID));
        assertThrows(ResourceNotFoundException.class, () -> useCase.entregar(OS_ID));
        verify(repository, never()).salvar(any());
    }

    @Test
    void aprovaOrcamentoComEstoqueSuficienteConsomeESalvaCadaMaterialEAOs() {
        OrdemServico os = osAprovacao();
        Material material = material(50L, BigDecimal.TEN);
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(materialRepository.buscarPorId(50L)).thenReturn(Optional.of(material));
        when(materialRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        OrdemServico resultado = useCase.aprovarOrcamento(OS_ID, 200L, "Aprovado");
        assertEquals(StatusOrdemServico.EM_EXECUCAO, resultado.getStatus());
        assertEquals(new BigDecimal("9"), material.getQuantidadeEstoque());
        verify(materialRepository).salvar(material);
        verify(repository).salvar(os);
    }

    @Test
    void bloqueiaAprovacaoComEstoqueInsuficienteSemConsumirNemSalvarMateriaisOuOs() {
        OrdemServico os = osAprovacao();
        Material material = spy(material(50L, BigDecimal.ZERO));
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(materialRepository.buscarPorId(50L)).thenReturn(Optional.of(material));
        DomainException erro = assertThrows(DomainException.class, () -> useCase.aprovarOrcamento(OS_ID, 200L, null));
        assertTrue(erro.getMessage().contains("Filtro"));
        verify(material, never()).consumir(any());
        verify(materialRepository, never()).salvar(any());
        verify(repository, never()).salvar(any());
    }

    @Test
    void listaTodosOsMateriaisInsuficientesNaMensagem() {
        OrdemServico os = osAprovacao();
        os.carregarMateriaisPersistidos(List.of(
                new ItemMaterial(101L, OS_ID, 50L, TipoMaterial.PECA, "Filtro", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN),
                new ItemMaterial(102L, OS_ID, 51L, TipoMaterial.INSUMO, "Oleo", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN)));
        Material filtro = spy(material(50L, BigDecimal.ZERO));
        Material oleo = spy(material(51L, BigDecimal.ZERO));
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(materialRepository.buscarPorId(50L)).thenReturn(Optional.of(filtro));
        when(materialRepository.buscarPorId(51L)).thenReturn(Optional.of(oleo));
        DomainException erro = assertThrows(DomainException.class, () -> useCase.aprovarOrcamento(OS_ID, 200L, null));
        assertTrue(erro.getMessage().contains("Filtro"));
        assertTrue(erro.getMessage().contains("Oleo"));
        verify(materialRepository, never()).salvar(any());
        verify(repository, never()).salvar(any());
        verify(filtro, never()).consumir(any());
        verify(oleo, never()).consumir(any());
    }

    @Test
    void interrompeQuandoMaterialDoItemNaoExisteSemSalvarMateriaisOuOs() {
        OrdemServico os = osAprovacao();
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        when(materialRepository.buscarPorId(50L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.aprovarOrcamento(OS_ID, 200L, null));
        verify(materialRepository, never()).salvar(any());
        verify(repository, never()).salvar(any());
    }

    @Test
    void repassaRegraDeAprovacaoInvalidaSemPersistir() {
        OrdemServico os = os(StatusOrdemServico.EM_DIAGNOSTICO);
        when(repository.buscarPorId(OS_ID)).thenReturn(Optional.of(os));
        assertThrows(DomainException.class, () -> useCase.aprovarOrcamento(OS_ID, 200L, null));
        verify(materialRepository, never()).salvar(any());
        verify(repository, never()).salvar(any());
    }

    private Servico servico() { return new Servico(40L, "S-40", "Reparo", null, BigDecimal.TEN, Duration.ofMinutes(1), DATA, true); }
    private Material material(Long id, BigDecimal estoque) { return new Material(id, "M-" + id, TipoMaterial.PECA, id == 50L ? "Filtro" : "Oleo", null, BigDecimal.TEN, "un", estoque, BigDecimal.ZERO, DATA, true); }
}

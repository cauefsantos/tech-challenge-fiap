package br.com.fiap.techchallengefiap.application.ordemservico;

import br.com.fiap.techchallengefiap.domain.cliente.model.Cliente;
import br.com.fiap.techchallengefiap.domain.cliente.model.TipoPessoa;
import br.com.fiap.techchallengefiap.domain.cliente.repository.ClienteRepository;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.OrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.model.StatusOrdemServico;
import br.com.fiap.techchallengefiap.domain.ordemservico.repository.OrdemServicoRepository;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import br.com.fiap.techchallengefiap.presentation.rest.ordemservico.PublicOrdemServicoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarOrdemServicoPublicaUseCaseTest {
    @Mock OrdemServicoRepository repository;
    @Mock ClienteRepository clienteRepository;
    @InjectMocks ConsultarOrdemServicoPublicaUseCase useCase;

    @Test
    void retornaSomenteDadosPublicosQuandoDocumentoConfereComOuSemMascara() {
        OrdemServico os = os();
        when(repository.buscarPorNumero("OS-1")).thenReturn(Optional.of(os));
        when(clienteRepository.buscarPorId(20L)).thenReturn(Optional.of(cliente()));
        PublicOrdemServicoResponse comMascara = useCase.executar("OS-1", "529.982.247-25");
        PublicOrdemServicoResponse semMascara = useCase.executar("OS-1", "52998224725");
        assertEquals(comMascara, semMascara);
        assertEquals("OS-1", comMascara.numero());
        assertEquals("RECEBIDA", comMascara.status());
        assertNull(comMascara.dataInicioExecucao());
        assertFalse(java.util.Arrays.stream(comMascara.getClass().getRecordComponents()).anyMatch(c ->
                java.util.List.of("documento", "nome", "email", "telefone").contains(c.getName())));
    }

    @Test
    void retornaMesmoTipoEMensagemDeAusenciaParaDocumentoDivergenteEOsInexistente() {
        when(repository.buscarPorNumero("OS-1")).thenReturn(Optional.of(os()), Optional.empty());
        when(clienteRepository.buscarPorId(20L)).thenReturn(Optional.of(cliente()));
        ResourceNotFoundException divergente = assertThrows(ResourceNotFoundException.class, () -> useCase.executar("OS-1", "39053344705"));
        ResourceNotFoundException inexistente = assertThrows(ResourceNotFoundException.class, () -> useCase.executar("OS-1", "52998224725"));
        assertEquals(divergente.getMessage(), inexistente.getMessage());
    }

    @Test
    void retornaAusenciaQuandoClienteDaOsNaoExiste() {
        when(repository.buscarPorNumero("OS-1")).thenReturn(Optional.of(os()));
        when(clienteRepository.buscarPorId(20L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.executar("OS-1", "52998224725"));
    }

    @Test
    void retornaAusenciaQuandoDocumentoOuOsNaoEncontrados() {
        when(repository.buscarPorNumero("OS-1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.executar("OS-1", "52998224725"));
        verifyNoInteractions(clienteRepository);
    }

    private OrdemServico os() { return new OrdemServico(10L, "OS-1", 20L, 30L, StatusOrdemServico.RECEBIDA, false, BigDecimal.ZERO, OffsetDateTime.parse("2024-01-01T00:00Z"), null, null, null, null, null, null); }
    private Cliente cliente() { return new Cliente(20L, TipoPessoa.FISICA, "52998224725", "Privado", "privado@example.com", "11999999999", OffsetDateTime.parse("2024-01-01T00:00Z"), true); }
}

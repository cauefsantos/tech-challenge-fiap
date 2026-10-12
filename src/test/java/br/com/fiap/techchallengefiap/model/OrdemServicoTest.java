package br.com.fiap.techchallengefiap.domain.ordemservico.model;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {
    @Test void deveExecutarFluxoPrincipal(){
        var os=new OrdemServico(1L,2L);
        os.enviarParaDiagnostico(); os.aguardarAprovacao(new BigDecimal("250.00")); os.aprovarOrcamento(); os.finalizar(); os.entregar();
        assertEquals(StatusOrdemServico.ENTREGUE,os.getStatus());
    }
    @Test void naoDeveFinalizarSemAprovacao(){
        var os=new OrdemServico(1L,2L);
        assertThrows(DomainException.class,os::finalizar);
    }
}
